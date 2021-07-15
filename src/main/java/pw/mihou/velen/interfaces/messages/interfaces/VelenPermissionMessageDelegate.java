package pw.mihou.velen.interfaces.messages.interfaces;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import pw.mihou.velen.interfaces.messages.types.VelenPermissionMessage;

import java.util.List;

public interface VelenPermissionMessageDelegate<R> extends VelenPermissionMessage {

    /**
     * Create a new Velen Message which will be used to retrieve
     * the message content of the rate-limited message, lack of permissions, role-locks, etc.
     * <p>
     * For example:
     * "You need to have these permissions: <b>permission</b> to use this command."
     *
     * @param permission The permissions needed.
     * @param user       The user.
     * @param channel    The text channel.
     * @param command    The command that the user executed.
     * @return The no-permission message.
     */
    R load(List<PermissionType> permission, User user, TextChannel channel, String command);

}
