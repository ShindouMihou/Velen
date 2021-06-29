package pw.mihou.velen.pagination;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.velen.pagination.entities.Paginator;
import pw.mihou.velen.pagination.events.PaginateEvent;

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
     * @param items The items to paginate through.
     * @param nextEmoji The emoji for next.
     * @param reverseEmoji The emoji for reverse.
     * @param selectEmoji The emoji for select.
     * @param cancelEmoji The emoji for cancel.
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
     * @param items The items to paginate through.
     * @param unicodeNext The unicode emoji for next.
     * @param unicodeReverse The unicode emoji for reverse.
     * @param unicodeSelect The unicode emoji for select.
     * @param unicodeCancel The unicode emoji for cancel.
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
     * @param event The MessageCreateEvent needed to start the event.
     * @param paginateEvent The handler for each paginate event.
     * @param removeAfter Remove the pagination event after (x) duration.
     */
    public void paginate(MessageCreateEvent event, PaginateEvent<T> paginateEvent, Duration removeAfter) {
        if(paginator.isEmpty()) {
            paginateEvent.onEmptyPaginator(event)
                    .replyTo(event.getMessage())
                    .send(event.getChannel())
                    .exceptionally(ExceptionLogger.get());
            return;
        }

        paginateEvent.onInit(event, paginator.current(), paginator.getArrow(), paginator)
                .replyTo(event.getMessage()).send(event.getChannel()).thenAccept(message -> {
                    if(nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
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
                        if(e.getUserId() != event.getApi().getYourself().getId()) {
                            e.removeReaction();
                        }

                        if(e.getUserId() != event.getMessageAuthor().getId())
                            return;

                        if(nextEmoji == null || reverseEmoji == null || selectEmoji == null || cancelEmoji == null) {
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

}
