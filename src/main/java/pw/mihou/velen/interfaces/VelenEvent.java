package pw.mihou.velen.interfaces;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

public interface VelenEvent {

    /**
     * This is called when the exact command is executed.
     *
     * @param event   The event received from Javacord.
     * @param message The message received from the event.
     * @param args    The arguments received from the event's message.
     * @param user    The user who executed the command.
     */
    void onEvent(MessageCreateEvent event, Message message, User user, String[] args);

}
