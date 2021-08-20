package pw.mihou.velen.pagination.events;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.pagination.entities.Paginator;
import pw.mihou.velen.pagination.interfaces.PaginationEvent;

public interface PaginateSimpleEvent<R, E, T> extends PaginationEvent<R, E, T> {

    /**
     * This is triggered whenever the user moves either a notch above or below. You should
     * handle the event here, for example, if the user moves a notch above, edit the paginate message
     * to change to the next item or the item behind.
     *
     * @param event           The event that triggered the pagination event.
     * @param paginateMessage The pagination message that was sent as a reply to the user.
     * @param currentItem     The current item.
     * @param arrow           The current arrow.
     * @param paginator       The paginator object.
     */
    void onPaginate(E event, Message paginateMessage, T currentItem, int arrow, Paginator<T> paginator);

}
