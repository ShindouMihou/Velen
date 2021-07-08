package pw.mihou.velen.builders;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;

public interface VelenGenericMessage {

    /**
     * Create a new Velen Message which will be used to retrieve
     * the message content of the rate-limited message, lack of permissions, role-locks, etc.
     * <p>
     * For example:
     * "You do not meet the required conditions: <b>something condition</b>"
     *
     * @param user    The user.
     * @param channel The text channel.
     * @param command The command that the user executed.
     * @return The no-permission message.
     */
    String load(User user, TextChannel channel, String command);

}
