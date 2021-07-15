package pw.mihou.velen.interfaces.messages.interfaces;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;
import pw.mihou.velen.interfaces.messages.types.VelenConditionalMessage;

public interface VelenConditionalMessageDelegate<R> extends VelenConditionalMessage {

    /**
     * Create a new Velen Message which will be used to retrieve
     * the message content of the "You do not meet the conditions" message.
     * <p>
     * For example:
     * "You do not meet the required conditions: <b>something condition</b>"
     *
     * @param user    The user.
     * @param channel The text channel.
     * @param command The command that the user executed.
     * @return The You do not meet the conditions message.
     */
    R load(User user, TextChannel channel, String command);

}
