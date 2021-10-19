package pw.mihou.velen.interfaces;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.interfaces.routed.VelenRoutedOptions;

public interface VelenEvent {

    /**
     * This is called when the exact command is executed.
     *
     * @param event   The event received from Javacord.
     * @param message The message received from the event.
     * @param args    The arguments received from the event's message.
     * @param user    The user who executed the command.
     * @param options The routed named options, requires formats to be set.
     */
    void onEvent(MessageCreateEvent event, Message message, User user, String[] args, VelenRoutedOptions options);

}
