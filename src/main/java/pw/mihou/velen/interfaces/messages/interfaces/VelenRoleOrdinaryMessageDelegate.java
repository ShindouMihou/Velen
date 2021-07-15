package pw.mihou.velen.interfaces.messages.interfaces;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;
import pw.mihou.velen.interfaces.messages.types.VelenRoleMessage;

public interface VelenRoleOrdinaryMessageDelegate<R> extends VelenRoleMessage {

    /**
     * Create a new Velen Message which will be used to retrieve
     * the message content of the rate-limited message, lack of permissions, role-locks, etc.
     * <p>
     * For example:
     * "You can need to have any of the roles: <b>roles</b> to use this command."
     *
     * @param roles   The roles needed (pre-formatted to mention-tag).
     * @param user    The user.
     * @param channel The text channel.
     * @param command The command that the user executed.
     * @return The no-permission message.
     */
    R load(String roles, User user, TextChannel channel, String command);

}
