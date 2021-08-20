package pw.mihou.velen.pagination.interfaces;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.pagination.entities.Paginator;

/**
 * This indicates that the event is of a Pagination event.
 */
public interface PaginationEvent<R, E, T> {

    /**
     * This is triggered on the creation of the paginator message. You
     * need to handle this event to start pagination.
     *
     * @param event       The event that triggered the pagination event.
     * @param currentItem The current item.
     * @param arrow       The current arrow.
     * @param paginator   The paginator object.
     * @return the initial response.
     */
    R onInit(E event, T currentItem, int arrow, Paginator<T> paginator);

    /**
     * This is triggered if the paginator is empty, usually because you didn't fill it up.
     * You should handle this event to notify the user that there were no results, etc.
     *
     * @param event The event that triggered the pagination event.
     * @return the response.
     */
    R onEmptyPaginator(E event);

    /**
     * This is triggered if the user cancels out of the pagination.
     * It is handled by default by deleting the messages.
     *
     * @param event   The event that triggered the pagination event.
     * @param message The pagination message that was sent as a reply to the user.
     */
    default void onCancel(E event, Message message) {
        message.delete();

        if(event instanceof MessageCreateEvent)
            ((MessageCreateEvent)event).getMessage().delete();
    }

}
