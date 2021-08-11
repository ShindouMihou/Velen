package pw.mihou.velen.pagination;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.velen.pagination.entities.Paginator;
import pw.mihou.velen.pagination.events.PaginateButtonEvent;
import pw.mihou.velen.pagination.events.PaginateButtonSimpleEvent;
import pw.mihou.velen.pagination.events.PaginateEvent;
import pw.mihou.velen.pagination.events.PaginateSimpleEvent;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Paginate<T> {

    private final Paginator<T> paginator;
    private Emoji nextEmoji;
    private Emoji reverseEmoji;
    private Emoji selectEmoji;
    private Emoji cancelEmoji;

    private String unicodeNext;
    private String unicodeReverse;
    private String unicodeSelect;
    private String unicodeCancel;

    /**
     * Creates a default pagination event that utilizes
     * the default unicode emojis.
     *
     * @param items The items to paginate through.
     */
    public Paginate(List<T> items) {
        this(items, "➡", "⬅", "👍", "👎");
    }

    /**
     * Creates a pagination event that utilizes emojis.
     *
     * @param items        The items to paginate through.
     * @param nextEmoji    The emoji for next.
     * @param reverseEmoji The emoji for reverse.
     * @param selectEmoji  The emoji for select.
     * @param cancelEmoji  The emoji for cancel.
     */
    public Paginate(List<T> items, Emoji nextEmoji, Emoji reverseEmoji, Emoji selectEmoji, Emoji cancelEmoji) {
        this.paginator = Paginator.of(items);
        this.nextEmoji = nextEmoji;
        this.reverseEmoji = reverseEmoji;
        this.selectEmoji = selectEmoji;
        this.cancelEmoji = cancelEmoji;
    }

    /**
     * Creates a pagination event that utilizes unicode emojis.
     *
     * @param items          The items to paginate through.
     * @param unicodeNext    The unicode emoji for next.
     * @param unicodeReverse The unicode emoji for reverse.
     * @param unicodeSelect  The unicode emoji for select.
     * @param unicodeCancel  The unicode emoji for cancel.
     */
    public Paginate(List<T> items, String unicodeNext, String unicodeReverse, String unicodeSelect, String unicodeCancel) {
        this.paginator = Paginator.of(items);
        this.unicodeNext = unicodeNext;
        this.unicodeReverse = unicodeReverse;
        this.unicodeSelect = unicodeSelect;
        this.unicodeCancel = unicodeCancel;
    }

    /**
     * Starts a pagination event.
     *
     * @param event         The MessageCreateEvent needed to start the event.
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter   Remove the pagination event after (x) duration.
     */
    public void paginate(MessageCreateEvent event, PaginateEvent<T> paginateEvent, Duration removeAfter) {
        if (paginator.isEmpty()) {
            paginateEvent.onEmptyPaginator(event)
                    .replyTo(event.getMessage())
                    .send(event.getChannel())
                    .exceptionally(ExceptionLogger.get());
            return;
        }

        paginateEvent.onInit(event, paginator.current(), paginator.getArrow(), paginator)
                .replyTo(event.getMessage()).send(event.getChannel()).thenAccept(message -> {
            if (nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
                if (paginator.size() > 1) {
                    message.addReactions(unicodeReverse, unicodeCancel, unicodeSelect, unicodeNext);
                } else {
                    message.addReactions(unicodeCancel, unicodeSelect);
                }
            } else {
                if (paginator.size() > 1) {
                    message.addReactions(reverseEmoji, cancelEmoji, selectEmoji, nextEmoji);
                } else {
                    message.addReactions(cancelEmoji, selectEmoji);
                }
            }

            message.addReactionAddListener(e -> {
                if (e.getUserId() != event.getApi().getYourself().getId()) {
                    e.removeReaction();
                }

                if (e.getUserId() != event.getMessageAuthor().getId())
                    return;

                if (nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
                    if (e.getEmoji().equalsEmoji(unicodeNext) && paginator.size() > 1) {
                        paginator.next().ifPresent(t -> paginateEvent.onPaginate(event, message, t, paginator.getArrow(), paginator));
                    } else if (e.getEmoji().equalsEmoji(unicodeReverse) && paginator.size() > 1) {
                        paginator.reverse().ifPresent(t -> paginateEvent.onPaginate(event, message, t, paginator.getArrow(), paginator));
                    } else if (e.getEmoji().equalsEmoji(unicodeSelect)) {
                        paginateEvent.onSelect(event, message, paginator.current(), paginator.getArrow(), paginator);
                    } else if (e.getEmoji().equalsEmoji(unicodeCancel)) {
                        paginateEvent.onCancel(event, message);
                    }
                } else {
                    if (e.getEmoji().equalsEmoji(nextEmoji) && paginator.size() > 1) {
                        paginator.next().ifPresent(t -> paginateEvent.onPaginate(event, message, t, paginator.getArrow(), paginator));
                    } else if (e.getEmoji().equalsEmoji(reverseEmoji) && paginator.size() > 1) {
                        paginator.reverse().ifPresent(t -> paginateEvent.onPaginate(event, message, t, paginator.getArrow(), paginator));
                    } else if (e.getEmoji().equalsEmoji(selectEmoji)) {
                        paginateEvent.onSelect(event, message, paginator.current(), paginator.getArrow(), paginator);
                    } else if (e.getEmoji().equalsEmoji(cancelEmoji)) {
                        paginateEvent.onCancel(event, message);
                    }
                }
            }).removeAfter(removeAfter.toMillis(), TimeUnit.MILLISECONDS).addRemoveHandler(message::removeAllReactions);
        }).exceptionally(ExceptionLogger.get());
    }

    /**
     * Starts a Pagination event that utilizes buttons instead
     * of reactions, <b>You can set removeAfter to a value of zero and the listener
     * will never remove itself (unless restart).</b>
     * <br><br>
     * Due to the limitations of Javacord at the moment, after <code>removeAfter</code> time has passed, only the
     * listener is removed and the buttons are kept there which is a bit sad but we can't do anything about
     * that until Javacord realizes a new update.
     *
     * @param uniqueId      The unique ID to use for this paginate event (it must be unique
     *                      unless you want conflicts to happen while listening for events).
     * @param event         The MessageCreateEvent needed to start the event.
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter   Remove the pagination event after (x) duration.
     */
    public void paginateWithButtons(String uniqueId, MessageCreateEvent event, PaginateButtonEvent<T> paginateEvent, Duration removeAfter) {
        if (paginator.isEmpty()) {
            paginateEvent.onEmptyPaginator(event)
                    .replyTo(event.getMessage())
                    .send(event.getChannel())
                    .exceptionally(ExceptionLogger.get());
            return;
        }

        MessageBuilder builder = paginateEvent.onInit(event, paginator.current(), paginator.getArrow(), paginator)
                .replyTo(event.getMessage());

        if (nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
            if (paginator.size() > 1) {
                builder.addActionRow(Button.primary(uniqueId + "-REVERSE", unicodeReverse),
                        Button.success(uniqueId + "-SUCCESS", unicodeSelect),
                        Button.danger(uniqueId + "-CANCEL", unicodeCancel),
                        Button.primary(uniqueId + "-NEXT", unicodeNext));
            } else {
                builder.addActionRow(Button.success(uniqueId + "-SUCCESS", unicodeSelect),
                        Button.danger(uniqueId + "-CANCEL", unicodeCancel));
            }
        } else {
            if (paginator.size() > 1) {
                builder.addActionRow(Button.primary(uniqueId + "-REVERSE", reverseEmoji),
                        Button.success(uniqueId + "-SUCCESS", selectEmoji),
                        Button.danger(uniqueId + "-CANCEL", cancelEmoji),
                        Button.primary(uniqueId + "-NEXT", nextEmoji));
            } else {
                builder.addActionRow(Button.success(uniqueId + "-SUCCESS", selectEmoji),
                        Button.danger(uniqueId + "-CANCEL", cancelEmoji));
            }
        }

        builder.send(event.getChannel()).thenAccept(message -> {
            ListenerManager<ButtonClickListener> listener = event.getApi().addButtonClickListener(e -> {
                String customId = e.getButtonInteraction().getCustomId();

                if (e.getButtonInteraction().getUser().getId() != event.getMessageAuthor().getId())
                    return;

                if (customId.equals(uniqueId + "-REVERSE"))
                    paginator.reverse().ifPresent(t -> paginateEvent.onPaginate(e.getButtonInteraction().createImmediateResponder(), event, message, t, paginator.getArrow(), paginator));

                if (customId.equals(uniqueId + "-NEXT"))
                    paginator.next().ifPresent(t -> paginateEvent.onPaginate(e.getButtonInteraction().createImmediateResponder(),
                            event, message, t, paginator.getArrow(), paginator));

                if (customId.equals(uniqueId + "-SUCCESS"))
                    paginateEvent.onSelect(e.getButtonInteraction().createImmediateResponder(), event, message, paginator.current(), paginator.getArrow(), paginator);

                if (customId.equals(uniqueId + "-CANCEL")) {
                    paginateEvent.onCancel(event, message);
                    e.getButtonInteraction().createImmediateResponder().respond();
                }
            });

            message.addMessageDeleteListener(e -> listener.remove());

            // We are adding this in case the user wants to automatically
            // free up the listener once time has passed.
            if (!removeAfter.isZero() && !removeAfter.isNegative())
                listener.removeAfter(removeAfter.toMillis(), TimeUnit.MILLISECONDS).addRemoveHandler(() -> new MessageUpdater(message)
                        .removeAllComponents()
                        .applyChanges());
        });
    }

    /**
     * Starts a Pagination event that utilizes buttons <b>(and without the select reaction) </b>instead
     * of reactions, <b>You can set removeAfter to a value of zero and the listener
     * will never remove itself (unless restart).</b>
     * <br><br>
     * Due to the limitations of Javacord at the moment, after <code>removeAfter</code> time has passed, only the
     * listener is removed and the buttons are kept there which is a bit sad but we can't do anything about
     * that until Javacord realizes a new update.
     *
     * @param uniqueId      The unique ID to use for this paginate event (it must be unique
     *                      unless you want conflicts to happen while listening for events).
     * @param event         The MessageCreateEvent needed to start the event.
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter   Remove the pagination event after (x) duration.
     */
    public void paginateWithButtons(String uniqueId, MessageCreateEvent event, PaginateButtonSimpleEvent<T> paginateEvent, Duration removeAfter) {
        if (paginator.isEmpty()) {
            paginateEvent.onEmptyPaginator(event)
                    .replyTo(event.getMessage())
                    .send(event.getChannel())
                    .exceptionally(ExceptionLogger.get());
            return;
        }

        MessageBuilder builder = paginateEvent.onInit(event, paginator.current(), paginator.getArrow(), paginator)
                .replyTo(event.getMessage());

        if (nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
            if (paginator.size() > 1) {
                builder.addActionRow(Button.primary(uniqueId + "-REVERSE", unicodeReverse),
                        Button.danger(uniqueId + "-CANCEL", unicodeCancel),
                        Button.primary(uniqueId + "-NEXT", unicodeNext));
            } else {
                builder.addActionRow(Button.danger(uniqueId + "-CANCEL", unicodeCancel));
            }
        } else {
            if (paginator.size() > 1) {
                builder.addActionRow(Button.primary(uniqueId + "-REVERSE", reverseEmoji),
                        Button.danger(uniqueId + "-CANCEL", cancelEmoji),
                        Button.primary(uniqueId + "-NEXT", nextEmoji));
            } else {
                builder.addActionRow(Button.success(uniqueId + "-SUCCESS", selectEmoji),
                        Button.danger(uniqueId + "-CANCEL", cancelEmoji));
            }
        }

        builder.send(event.getChannel()).thenAccept(message -> {
            ListenerManager<ButtonClickListener> listener = event.getApi().addButtonClickListener(e -> {
                String customId = e.getButtonInteraction().getCustomId();

                if (e.getButtonInteraction().getUser().getId() != event.getMessageAuthor().getId())
                    return;

                if (customId.equals(uniqueId + "-REVERSE"))
                    paginator.reverse().ifPresent(t -> paginateEvent.onPaginate(e.getButtonInteraction().createImmediateResponder(), event, message, t, paginator.getArrow(), paginator));

                if (customId.equals(uniqueId + "-NEXT"))
                    paginator.next().ifPresent(t -> paginateEvent.onPaginate(e.getButtonInteraction().createImmediateResponder(),
                            event, message, t, paginator.getArrow(), paginator));

                if (customId.equals(uniqueId + "-CANCEL")) {
                    paginateEvent.onCancel(event, message);
                    e.getButtonInteraction().createImmediateResponder().respond();
                }
            });

            message.addMessageDeleteListener(e -> listener.remove());

            // We are adding this in case the user wants to automatically
            // free up the listener once time has passed.
            if (!removeAfter.isZero() && !removeAfter.isNegative())
                listener.removeAfter(removeAfter.toMillis(), TimeUnit.MILLISECONDS).addRemoveHandler(() -> new MessageUpdater(message)
                        .removeAllComponents()
                        .applyChanges());
        });
    }

    /**
     * Starts a pagination event <b>but without the select reaction</b>,
     * this is usually handy for cases where you only want the user to look
     * through certain pages.
     *
     * @param event         The MessageCreateEvent needed to start the event.
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter   Remove the pagination event after (x) duration.
     */
    public void paginate(MessageCreateEvent event, PaginateSimpleEvent<T> paginateEvent, Duration removeAfter) {
        if (paginator.isEmpty()) {
            paginateEvent.onEmptyPaginator(event)
                    .replyTo(event.getMessage())
                    .send(event.getChannel())
                    .exceptionally(ExceptionLogger.get());
            return;
        }

        paginateEvent.onInit(event, paginator.current(), paginator.getArrow(), paginator)
                .replyTo(event.getMessage()).send(event.getChannel()).thenAccept(message -> {
            if (nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
                if (paginator.size() > 1) {
                    message.addReactions(unicodeReverse, unicodeCancel, unicodeNext);
                } else {
                    message.addReactions(unicodeCancel);
                }
            } else {
                if (paginator.size() > 1) {
                    message.addReactions(reverseEmoji, cancelEmoji, nextEmoji);
                } else {
                    message.addReactions(cancelEmoji);
                }
            }

            message.addReactionAddListener(e -> {
                if (e.getUserId() != event.getApi().getYourself().getId()) {
                    e.removeReaction();
                }

                if (e.getUserId() != event.getMessageAuthor().getId())
                    return;

                if (nextEmoji == null || reverseEmoji == null || cancelEmoji == null) {
                    if (e.getEmoji().equalsEmoji(unicodeNext) && paginator.size() > 1) {
                        paginator.next().ifPresent(t -> paginateEvent.onPaginate(event, message, t, paginator.getArrow(), paginator));
                    } else if (e.getEmoji().equalsEmoji(unicodeReverse) && paginator.size() > 1) {
                        paginator.reverse().ifPresent(t -> paginateEvent.onPaginate(event, message, t, paginator.getArrow(), paginator));
                    } else if (e.getEmoji().equalsEmoji(unicodeCancel)) {
                        paginateEvent.onCancel(event, message);
                    }
                } else {
                    if (e.getEmoji().equalsEmoji(nextEmoji) && paginator.size() > 1) {
                        paginator.next().ifPresent(t -> paginateEvent.onPaginate(event, message, t, paginator.getArrow(), paginator));
                    } else if (e.getEmoji().equalsEmoji(reverseEmoji) && paginator.size() > 1) {
                        paginator.reverse().ifPresent(t -> paginateEvent.onPaginate(event, message, t, paginator.getArrow(), paginator));
                    } else if (e.getEmoji().equalsEmoji(cancelEmoji)) {
                        paginateEvent.onCancel(event, message);
                    }
                }
            }).removeAfter(removeAfter.toMillis(), TimeUnit.MILLISECONDS).addRemoveHandler(message::removeAllReactions);
        }).exceptionally(ExceptionLogger.get());
    }

}
