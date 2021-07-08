package pw.mihou.velen.pagination.events;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.pagination.entities.Paginator;

public interface PaginateSimpleEvent<T> {

    /**
     * This is triggered on the creation of the paginator message. You
     * need to handle this event to start pagination.
     *
     * @param event       The MessageCreateEvent that triggered the pagination event.
     * @param currentItem The current item.
     * @param arrow       The current arrow.
     * @param paginator   The paginator object.
     * @return the initial response.
     */
    MessageBuilder onInit(MessageCreateEvent event, T currentItem, int arrow, Paginator<T> paginator);

    /**
     * This is triggered whenever the user moves either a notch above or below. You should
     * handle the event here, for example, if the user moves a notch above, edit the paginate message
     * to change to the next item or the item behind.
     *
     * @param event           The MessageCreateEvent that triggered the pagination event.
     * @param paginateMessage The pagination message that was sent as a reply to the user.
     * @param currentItem     The current item.
     * @param arrow           The current arrow.
     * @param paginator       The paginator object.
     */
    void onPaginate(MessageCreateEvent event, Message paginateMessage, T currentItem, int arrow, Paginator<T> paginator);

    /**
     * This is triggered if the paginator is empty, usually because you didn't fill it up.
     * You should handle this event to notify the user that there were no results, etc.
     *
     * @param event The MessageCreateEvent that triggered the pagination event.
     * @return the response.
     */
    MessageBuilder onEmptyPaginator(MessageCreateEvent event);

    /**
     * This is triggered if the user cancels out of the pagination.
     * It is handled by default by deleting the messages.
     *
     * @param event   The MessageCreateEvent that triggered the pagination event.
     * @param message The pagination message that was sent as a reply to the user.
     */
    default void onCancel(MessageCreateEvent event, Message message) {
        message.delete();
        event.getMessage().delete();
    }

}
