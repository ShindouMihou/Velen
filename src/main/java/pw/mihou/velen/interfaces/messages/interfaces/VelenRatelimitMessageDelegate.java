package pw.mihou.velen.interfaces.messages.interfaces;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;
import pw.mihou.velen.interfaces.messages.types.VelenRatelimitMessage;

public interface VelenRatelimitMessageDelegate<R> extends VelenRatelimitMessage {

    /**
     * Create a new Velen Message which will be used to retrieve
     * the message content of the rate-limited message, lack of permissions, role-locks, etc.
     * <p>
     * For example:
     * "You can use this command in <b>remaining seconds</b>, during this period,
     * the bot will not respond to any invocation of the command: <b>command</b> for the user.
     * This message will delete itself when cooldown is over."
     *
     * @param remainingSeconds The amount of seconds remaining before cooldown clears.
     * @param user             The user who got rate-limited.
     * @param channel          The text channel where the user was rate-limited.
     * @param command          The command that the user is rate-limited on.
     * @return The rate-limited message.
     */
    R load(long remainingSeconds, User user, TextChannel channel, String command);

}
