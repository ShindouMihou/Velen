package pw.mihou.velen.pagination.events;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.pagination.entities.Paginator;
import pw.mihou.velen.pagination.interfaces.PaginationEvent;

public interface PaginateEvent<R, E, T> extends PaginationEvent<R, E, T>, PaginateSimpleEvent<R, E, T> {

    /**
     * This is triggered if the user selects a page with the emoji that is used to
     * select what page they want.
     *
     * @param event           The event that triggered the pagination event.
     * @param paginateMessage The pagination message that was sent as a reply to the user.
     * @param itemSelected    The current item.
     * @param arrow           The current arrow.
     * @param paginator       The paginator object.
     */
    void onSelect(E event, Message paginateMessage, T itemSelected, int arrow, Paginator<T> paginator);

}
