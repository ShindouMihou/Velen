package pw.mihou.velen.pagination;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.velen.pagination.entities.Paginator;
import pw.mihou.velen.pagination.events.PaginateButtonEvent;
import pw.mihou.velen.pagination.events.PaginateButtonSimpleEvent;
import pw.mihou.velen.pagination.events.PaginateEvent;
import pw.mihou.velen.pagination.events.PaginateSimpleEvent;
import pw.mihou.velen.pagination.interfaces.PaginationEvent;

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
    public void paginate(MessageCreateEvent event, PaginateEvent<MessageBuilder, MessageCreateEvent, T> paginateEvent, Duration removeAfter) {
        startEvent(true, event, paginateEvent, removeAfter);
    }

    /**
     * Starts a pagination event,
     * this is usually handy for cases where you only want the user to look
     * through certain pages.
     *
     * @param event         The interaction event needed to start the event. (this needs to be an interaction that has a channel).
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter   Remove the pagination event after (x) duration.
     */
    public void paginate(Interaction event, PaginateEvent<InteractionOriginalResponseUpdater, Interaction, T> paginateEvent, Duration removeAfter) {
        startEvent(true, event, paginateEvent, removeAfter);
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
    public void paginate(MessageCreateEvent event, PaginateSimpleEvent<MessageBuilder, MessageCreateEvent, T> paginateEvent, Duration removeAfter) {
        startEvent(false, event, paginateEvent, removeAfter);
    }

    /**
     * Starts a pagination event <b>but without the select reaction</b>,
     * this is usually handy for cases where you only want the user to look
     * through certain pages.
     *
     * @param event         The interaction event needed to start the event. (this needs to be an interaction that has a channel).
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter   Remove the pagination event after (x) duration.
     */
    public void paginate(Interaction event, PaginateSimpleEvent<InteractionOriginalResponseUpdater, Interaction, T> paginateEvent, Duration removeAfter) {
        startEvent(false, event, paginateEvent, removeAfter);
    }

    /**
     * Starts a Pagination event that utilizes buttons instead
     * of reactions, <b>You can set removeAfter to a value of zero and the listener
     * will never remove itself (unless restart).</b>
     * <br><br>
     *
     * @param uniqueId      The unique ID to use for this paginate event (it must be unique
     *                      unless you want conflicts to happen while listening for events).
     * @param event         The MessageCreateEvent needed to start the event.
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter   Remove the pagination event after (x) duration.
     */
    public void paginateWithButtons(String uniqueId, MessageCreateEvent event, PaginateButtonEvent<MessageBuilder, MessageCreateEvent, T> paginateEvent, Duration removeAfter) {
        startEvent(true, uniqueId, event, paginateEvent, removeAfter);
    }

    /**
     * Starts a Pagination event that utilizes buttons <b>(and without the select reaction) </b>instead
     * of reactions, <b>You can set removeAfter to a value of zero and the listener
     * will never remove itself (unless restart).</b>
     *
     * @param uniqueId      The unique ID to use for this paginate event (it must be unique
     *                      unless you want conflicts to happen while listening for events).
     * @param event         The MessageCreateEvent needed to start the event.
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter   Remove the pagination event after (x) duration.
     */
    public void paginateWithButtons(String uniqueId, MessageCreateEvent event, PaginateButtonSimpleEvent<MessageBuilder, MessageCreateEvent, T> paginateEvent, Duration removeAfter) {
        startEvent(false, uniqueId, event, paginateEvent, removeAfter);
    }

    /**
     * Starts a Pagination event that utilizes buttons <b>(and without the select reaction) </b>instead
     * of reactions, <b>You can set removeAfter to a value of zero and the listener
     * will never remove itself (unless restart).</b>
     *
     * @param uniqueId      The unique ID to use for this paginate event (it must be unique
     *                      unless you want conflicts to happen while listening for events).
     * @param event         The interaction event needed to start the event.
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter   Remove the pagination event after (x) duration.
     */
    public void paginateWithButtons(String uniqueId, Interaction event,
                                    PaginateButtonSimpleEvent<InteractionOriginalResponseUpdater, Interaction, T> paginateEvent,
                                    Duration removeAfter) {
        startEvent(false, uniqueId, event, paginateEvent, removeAfter);
    }

    /**
     * Starts a Pagination event that utilizes buttons instead
     * of reactions, <b>You can set removeAfter to a value of zero and the listener
     * will never remove itself (unless restart).</b>
     *
     * @param uniqueId      The unique ID to use for this paginate event (it must be unique
     *                      unless you want conflicts to happen while listening for events).
     * @param event         The interaction event needed to start the event.
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter   Remove the pagination event after (x) duration.
     */
    public void paginateWithButtons(String uniqueId, Interaction event,
                                    PaginateButtonEvent<InteractionOriginalResponseUpdater, Interaction, T> paginateEvent,
                                    Duration removeAfter) {
        startEvent(true, uniqueId, event, paginateEvent, removeAfter);
    }

    //
    // Here starts the hell part.
    // Beware, your brain will fry soon.
    //

    private void startEvent(boolean includeSelect, String uniqueId, MessageCreateEvent event, PaginateButtonSimpleEvent<MessageBuilder, MessageCreateEvent, T> paginateEvent, Duration removeAfter) {
        if (paginator.isEmpty()) {
            paginateEvent.onEmptyPaginator(event)
                    .replyTo(event.getMessage())
                    .send(event.getChannel())
                    .exceptionally(ExceptionLogger.get());
            return;
        }

        MessageBuilder builder = addButtons(includeSelect,
                paginateEvent.onInit(event, paginator.current(), paginator.getArrow(), paginator).replyTo(event.getMessage()), uniqueId);

        builder.send(event.getChannel()).thenAccept(message -> {
            ListenerManager<ButtonClickListener> listener = message
                    .addButtonClickListener(e -> handlePaginateButtonEventMessage(uniqueId, e, message, event, paginateEvent));

            message.addMessageDeleteListener(e -> listener.remove());

            // We are adding this in case the user wants to automatically
            // free up the listener once time has passed.
            if (!removeAfter.isZero() && !removeAfter.isNegative())
                listener.removeAfter(removeAfter.toMillis(), TimeUnit.MILLISECONDS).addRemoveHandler(() -> new MessageUpdater(message)
                        .removeAllComponents()
                        .applyChanges());
        });
    }

    private void startEvent(boolean includeSelect, String uniqueId, Interaction event,
                            PaginateButtonSimpleEvent<InteractionOriginalResponseUpdater, Interaction, T> paginateEvent, Duration removeAfter) {
        if (paginator.isEmpty()) {
            paginateEvent.onEmptyPaginator(event)
                    .update().exceptionally(ExceptionLogger.get());
            return;
        }


        InteractionOriginalResponseUpdater builder = addButtons(includeSelect,
                paginateEvent.onInit(event, paginator.current(), paginator.getArrow(), paginator), uniqueId);

        builder.update().thenAccept(message -> {
            ListenerManager<ButtonClickListener> listener = message
                    .addButtonClickListener(e -> handlePaginateButtonEventInteraction(uniqueId, e, message, event, paginateEvent));

            message.addMessageDeleteListener(e -> listener.remove());

            // We are adding this in case the user wants to automatically
            // free up the listener once time has passed.
            if (!removeAfter.isZero() && !removeAfter.isNegative())
                listener.removeAfter(removeAfter.toMillis(), TimeUnit.MILLISECONDS).addRemoveHandler(() -> new MessageUpdater(message)
                        .removeAllComponents()
                        .applyChanges());
        });
    }

    private void startEvent(boolean includeSelect, MessageCreateEvent event,
                            PaginationEvent<MessageBuilder, MessageCreateEvent, T> paginateEvent,
                            Duration removeAfter) {
        if (paginator.isEmpty()) {
            paginateEvent.onEmptyPaginator(event)
                    .replyTo(event.getMessage())
                    .send(event.getChannel())
                    .exceptionally(ExceptionLogger.get());
            return;
        }

        paginateEvent.onInit(event, paginator.current(), paginator.getArrow(), paginator)
                .replyTo(event.getMessage()).send(event.getChannel()).thenAccept(message -> {
                    addReactions(includeSelect, message);

                    message.addReactionAddListener(e -> handlePaginateReactionEventMessage(e, event, paginateEvent, message))
                            .removeAfter(removeAfter.toMillis(), TimeUnit.MILLISECONDS).addRemoveHandler(message::removeAllReactions);
                }).exceptionally(ExceptionLogger.get());
    }

    private void startEvent(boolean includeSelected, Interaction event, PaginateSimpleEvent<InteractionOriginalResponseUpdater,
            Interaction, T> paginateEvent, Duration removeAfter) {
        if(paginator.isEmpty()) {
            paginateEvent.onEmptyPaginator(event)
                    .update().exceptionally(ExceptionLogger.get());
            return;
        }

        paginateEvent.onInit(event, paginator.current(), paginator.getArrow(), paginator)
                .update().thenAccept(message -> {
                    addReactions(includeSelected, message);

                    message.addReactionAddListener(e -> handlePaginateReactionEventInteraction(e, event, paginateEvent, message))
                            .removeAfter(removeAfter.toMillis(), TimeUnit.MILLISECONDS).addRemoveHandler(message::removeAllReactions);
                }).exceptionally(ExceptionLogger.get());
    }

    //
    // Handle reaction events
    // from both interaction and message events.
    //

    private void handlePaginateReactionEventMessage(ReactionAddEvent e, MessageCreateEvent event,
                                                    PaginationEvent<MessageBuilder, MessageCreateEvent, T> paginateEvent, Message message) {
        if (e.getUserId() != event.getApi().getYourself().getId()) {
            e.removeReaction();
        }

        if (e.getUserId() != event.getMessageAuthor().getId())
            return;

        if (nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
            if (e.getEmoji().equalsEmoji(unicodeNext) && paginator.size() > 1) {
                paginator.next().ifPresent(t -> ((PaginateSimpleEvent<MessageBuilder, MessageCreateEvent, T>) paginateEvent)
                        .onPaginate(event, message, t, paginator.getArrow(), paginator));
            } else if (e.getEmoji().equalsEmoji(unicodeReverse) && paginator.size() > 1) {
                paginator.reverse().ifPresent(t -> ((PaginateSimpleEvent<MessageBuilder, MessageCreateEvent, T>) paginateEvent)
                        .onPaginate(event, message, t, paginator.getArrow(), paginator));
            } else if (e.getEmoji().equalsEmoji(unicodeSelect) && paginateEvent instanceof PaginateEvent) {
                ((PaginateEvent<MessageBuilder, MessageCreateEvent, T>) paginateEvent)
                        .onSelect(event, message, paginator.current(), paginator.getArrow(), paginator);
            } else if (e.getEmoji().equalsEmoji(unicodeCancel)) {
                paginateEvent.onCancel(event, message);
            }
        } else {
            if (e.getEmoji().equalsEmoji(nextEmoji) && paginator.size() > 1) {
                paginator.next().ifPresent(t -> ((PaginateSimpleEvent<MessageBuilder, MessageCreateEvent, T>) paginateEvent)
                        .onPaginate(event, message, t, paginator.getArrow(), paginator));
            } else if (e.getEmoji().equalsEmoji(reverseEmoji) && paginator.size() > 1) {
                paginator.reverse().ifPresent(t -> ((PaginateSimpleEvent<MessageBuilder, MessageCreateEvent, T>) paginateEvent)
                        .onPaginate(event, message, t, paginator.getArrow(), paginator));
            } else if (e.getEmoji().equalsEmoji(selectEmoji) && paginateEvent instanceof PaginateEvent) {
                ((PaginateEvent<MessageBuilder, MessageCreateEvent, T>) paginateEvent)
                        .onSelect(event, message, paginator.current(), paginator.getArrow(), paginator);
            } else if (e.getEmoji().equalsEmoji(cancelEmoji)) {
                paginateEvent.onCancel(event, message);
            }
        }
    }

    private void handlePaginateReactionEventInteraction(ReactionAddEvent e, Interaction event,
                                                        PaginationEvent<InteractionOriginalResponseUpdater, Interaction, T> paginateEvent, Message message) {
        if (e.getUserId() != event.getApi().getYourself().getId()) {
            e.removeReaction();
        }

        if (e.getUserId() != event.getUser().getId())
            return;


        if (nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
            if (e.getEmoji().equalsEmoji(unicodeNext) && paginator.size() > 1) {
                paginator.next().ifPresent(t -> ((PaginateSimpleEvent<InteractionOriginalResponseUpdater, Interaction, T>) paginateEvent)
                        .onPaginate(event, message, t, paginator.getArrow(), paginator));
            } else if (e.getEmoji().equalsEmoji(unicodeReverse) && paginator.size() > 1) {
                paginator.reverse().ifPresent(t -> ((PaginateSimpleEvent<InteractionOriginalResponseUpdater, Interaction, T>) paginateEvent)
                        .onPaginate(event, message, t, paginator.getArrow(), paginator));
            } else if (e.getEmoji().equalsEmoji(unicodeSelect) && paginateEvent instanceof PaginateEvent) {
                ((PaginateEvent<InteractionOriginalResponseUpdater, Interaction, T>) paginateEvent)
                        .onSelect(event, message, paginator.current(), paginator.getArrow(), paginator);
            } else if (e.getEmoji().equalsEmoji(unicodeCancel)) {
                paginateEvent.onCancel(event, message);
            }
        } else {
            if (e.getEmoji().equalsEmoji(nextEmoji) && paginator.size() > 1) {
                paginator.next().ifPresent(t -> ((PaginateSimpleEvent<InteractionOriginalResponseUpdater, Interaction, T>) paginateEvent)
                        .onPaginate(event, message, t, paginator.getArrow(), paginator));
            } else if (e.getEmoji().equalsEmoji(reverseEmoji) && paginator.size() > 1) {
                paginator.reverse().ifPresent(t -> ((PaginateSimpleEvent<InteractionOriginalResponseUpdater, Interaction, T>) paginateEvent)
                        .onPaginate(event, message, t, paginator.getArrow(), paginator));
            } else if (e.getEmoji().equalsEmoji(selectEmoji) && paginateEvent instanceof PaginateEvent) {
                ((PaginateEvent<InteractionOriginalResponseUpdater, Interaction, T>) paginateEvent)
                        .onSelect(event, message, paginator.current(), paginator.getArrow(), paginator);
            } else if (e.getEmoji().equalsEmoji(cancelEmoji)) {
                paginateEvent.onCancel(event, message);
            }
        }
    }

    //
    // Handle button events from
    // both message and interaction events.
    //

    private void handlePaginateButtonEventMessage(String uniqueId, ButtonClickEvent e,
                                                  Message message, MessageCreateEvent event,
                                                  PaginateButtonSimpleEvent<MessageBuilder, MessageCreateEvent, T> paginateEvent) {
        String customId = e.getButtonInteraction().getCustomId();

        if (e.getButtonInteraction().getUser().getId() != event.getMessageAuthor().getId())
            return;

        if (customId.equals(uniqueId + "-REVERSE"))
            paginator.reverse().ifPresent(t -> paginateEvent.onPaginate(e.getButtonInteraction().createImmediateResponder(),
                    event, message, t, paginator.getArrow(), paginator));

        if (customId.equals(uniqueId + "-NEXT"))
            paginator.next().ifPresent(t -> paginateEvent.onPaginate(e.getButtonInteraction().createImmediateResponder(),
                    event, message, t, paginator.getArrow(), paginator));

        if (customId.equals(uniqueId + "-SUCCESS") && paginateEvent instanceof PaginateButtonEvent)
            ((PaginateButtonEvent<MessageBuilder, MessageCreateEvent, T>)paginateEvent)
                    .onSelect(e.getButtonInteraction().createImmediateResponder(), event, message, paginator.current(),
                            paginator.getArrow(), paginator);

        if (customId.equals(uniqueId + "-CANCEL")) {
            paginateEvent.onCancel(event, message);
            e.getButtonInteraction().createImmediateResponder().respond();
        }
    }

    private void handlePaginateButtonEventInteraction(String uniqueId, ButtonClickEvent e,
                                                      Message message, Interaction event,
                                                      PaginateButtonSimpleEvent<InteractionOriginalResponseUpdater, Interaction, T>
                                                              paginateEvent) {
        String customId = e.getButtonInteraction().getCustomId();

        if (e.getButtonInteraction().getUser().getId() != event.getUser().getId())
            return;

        if (customId.equals(uniqueId + "-REVERSE"))
            paginator.reverse().ifPresent(t -> paginateEvent.onPaginate(e.getButtonInteraction().createImmediateResponder(),
                    event, message, t, paginator.getArrow(), paginator));

        if (customId.equals(uniqueId + "-NEXT"))
            paginator.next().ifPresent(t -> paginateEvent.onPaginate(e.getButtonInteraction().createImmediateResponder(),
                    event, message, t, paginator.getArrow(), paginator));

        if (customId.equals(uniqueId + "-SUCCESS") && paginateEvent instanceof PaginateButtonEvent)
            ((PaginateButtonEvent<InteractionOriginalResponseUpdater, Interaction, T>)paginateEvent)
                    .onSelect(e.getButtonInteraction().createImmediateResponder(), event, message, paginator.current(),
                            paginator.getArrow(), paginator);

        if (customId.equals(uniqueId + "-CANCEL")) {
            paginateEvent.onCancel(event, message);
            e.getButtonInteraction().createImmediateResponder().respond();
        }
    }

    //
    // Handle add reactions
    // and adding of buttons.
    //

    private InteractionOriginalResponseUpdater addButtons(boolean includeSelect, InteractionOriginalResponseUpdater builder, String uniqueId) {
        if (nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
            if (paginator.size() > 1) {
                if(includeSelect)
                    builder.addComponents(ActionRow.of(Button.primary(uniqueId + "-REVERSE", unicodeReverse),
                            Button.success(uniqueId + "-SUCCESS", unicodeSelect),
                            Button.danger(uniqueId + "-CANCEL", unicodeCancel),
                            Button.primary(uniqueId + "-NEXT", unicodeNext)));
                else
                    builder.addComponents(ActionRow.of(Button.primary(uniqueId + "-REVERSE", unicodeReverse),
                            Button.danger(uniqueId + "-CANCEL", unicodeCancel),
                            Button.primary(uniqueId + "-NEXT", unicodeNext)));
            } else {
                if(includeSelect)
                    builder.addComponents(ActionRow.of(Button.success(uniqueId + "-SUCCESS", unicodeSelect),
                            Button.danger(uniqueId + "-CANCEL", unicodeCancel)));
                else
                    builder.addComponents(ActionRow.of(Button.danger(uniqueId + "-CANCEL", unicodeCancel)));
            }
        } else {
            if (paginator.size() > 1) {
                if(includeSelect)
                    builder.addComponents(ActionRow.of(Button.primary(uniqueId + "-REVERSE", reverseEmoji),
                            Button.success(uniqueId + "-SUCCESS", selectEmoji),
                            Button.danger(uniqueId + "-CANCEL", cancelEmoji),
                            Button.primary(uniqueId + "-NEXT", nextEmoji)));
                else
                    builder.addComponents(ActionRow.of(Button.primary(uniqueId + "-REVERSE", unicodeReverse),
                            Button.danger(uniqueId + "-CANCEL", unicodeCancel),
                            Button.primary(uniqueId + "-NEXT", unicodeNext)));
            } else {
                if(includeSelect)
                    builder.addComponents(ActionRow.of(Button.success(uniqueId + "-SUCCESS", selectEmoji),
                            Button.danger(uniqueId + "-CANCEL", cancelEmoji)));
                else
                    builder.addComponents(ActionRow.of(Button.danger(uniqueId + "-CANCEL", cancelEmoji)));
            }
        }

        return builder;
    }

    private MessageBuilder addButtons(boolean includeSelect, MessageBuilder builder, String uniqueId) {
        if (nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
            if (paginator.size() > 1) {
                if(includeSelect)
                    builder.addActionRow(Button.primary(uniqueId + "-REVERSE", unicodeReverse),
                            Button.success(uniqueId + "-SUCCESS", unicodeSelect),
                            Button.danger(uniqueId + "-CANCEL", unicodeCancel),
                            Button.primary(uniqueId + "-NEXT", unicodeNext));
                else
                    builder.addActionRow(Button.primary(uniqueId + "-REVERSE", unicodeReverse),
                            Button.danger(uniqueId + "-CANCEL", unicodeCancel),
                            Button.primary(uniqueId + "-NEXT", unicodeNext));
            } else {
                if(includeSelect)
                    builder.addActionRow(Button.success(uniqueId + "-SUCCESS", unicodeSelect),
                            Button.danger(uniqueId + "-CANCEL", unicodeCancel));
                else
                    builder.addActionRow(Button.danger(uniqueId + "-CANCEL", unicodeCancel));
            }
        } else {
            if (paginator.size() > 1) {
                if(includeSelect)
                    builder.addActionRow(Button.primary(uniqueId + "-REVERSE", reverseEmoji),
                            Button.success(uniqueId + "-SUCCESS", selectEmoji),
                            Button.danger(uniqueId + "-CANCEL", cancelEmoji),
                            Button.primary(uniqueId + "-NEXT", nextEmoji));
                else
                    builder.addActionRow(Button.primary(uniqueId + "-REVERSE", unicodeReverse),
                            Button.danger(uniqueId + "-CANCEL", unicodeCancel),
                            Button.primary(uniqueId + "-NEXT", unicodeNext));
            } else {
                if(includeSelect)
                    builder.addActionRow(Button.success(uniqueId + "-SUCCESS", selectEmoji),
                            Button.danger(uniqueId + "-CANCEL", cancelEmoji));
                else
                    builder.addActionRow(Button.danger(uniqueId + "-CANCEL", cancelEmoji));
            }
        }

        return builder;
    }

    private void addReactions(boolean includeSelect, Message message) {
        if (nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
            if (paginator.size() > 1) {
                if (includeSelect)
                    message.addReactions(unicodeReverse, unicodeCancel, unicodeSelect, unicodeNext);
                else
                    message.addReactions(unicodeReverse, unicodeCancel, unicodeNext);
            } else {
                if (includeSelect)
                    message.addReactions(unicodeCancel, unicodeSelect);
                else
                    message.addReactions(unicodeCancel);
            }
        } else {
            if (paginator.size() > 1) {
                if (includeSelect)
                    message.addReactions(reverseEmoji, cancelEmoji, selectEmoji, nextEmoji);
                else
                    message.addReactions(reverseEmoji, cancelEmoji, nextEmoji);
            } else {
                if (includeSelect)
                    message.addReactions(cancelEmoji, selectEmoji);
                else
                    message.addReactions(cancelEmoji);
            }
        }
    }

}
