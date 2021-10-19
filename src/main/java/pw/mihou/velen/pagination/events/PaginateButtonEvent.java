package pw.mihou.velen.pagination.events;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pw.mihou.velen.pagination.entities.Paginator;
import pw.mihou.velen.pagination.interfaces.PaginationEvent;

public interface PaginateButtonEvent<R, E, T> extends PaginationEvent<R, E, T>, PaginateButtonSimpleEvent<R, E, T> {

    /**
     * This is triggered if the user selects a page with the emoji that is used to
     * select what page they want.
     *
     * @param responder       The response the bot should send to the client (required).
     * @param event           The event that triggered the pagination event.
     * @param paginateMessage The pagination message that was sent as a reply to the user.
     * @param itemSelected    The current item.
     * @param arrow           The current arrow.
     * @param paginator       The paginator object.
     */
    void onSelect(InteractionImmediateResponseBuilder responder,
                  E event, Message paginateMessage, T itemSelected, int arrow, Paginator<T> paginator);

}
