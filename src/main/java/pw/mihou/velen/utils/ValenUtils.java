package pw.mihou.velen.utils;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.DiscordRegexPattern;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.internals.VelenInternalUtils;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class ValenUtils {

    private static final VelenInternalUtils internals;

    static {
        internals = new VelenInternalUtils(Locale.ENGLISH);
    }

    /**
     * Parses the messages and returns the correct order of users mentioned.
     *
     * @param message The message to parse.
     * @return A correctly ordered list of user mentions.
     */
    public static Collection<Long> getOrderedUserMentions(String message) {
        List<Long> users = new ArrayList<>();
        Matcher matcher = DiscordRegexPattern.USER_MENTION.matcher(message);
        while (matcher.find()) {
            users.add(Long.parseLong(matcher.group()
                    .replaceFirst("<@!", "")
                    .replaceFirst("<@", "")
                    .replaceFirst(">", "")));
        }
        return users;
    }

    /**
     * Parses the messages and returns the correct order of users mentioned.
     *
     * @param api The Discord API to use.
     * @param message The message to parse.
     * @return A correctly ordered list of channel mentions.
     */
    public static CompletableFuture<Collection<User>> getOrderedUserMentions(DiscordApi api, String message) {
        return CompletableFuture.supplyAsync(() -> getOrderedUserMentions(message).stream().map(api::getUserById)
                .map(CompletableFuture::join).collect(Collectors.toList()));
    }

    /**
     * Parses the messages and returns the correct order of roles mentioned.
     *
     * @param message The message to parse.
     * @return A correctly ordered list of roles mentions.
     */
    public static Collection<Long> getOrderedRoleMentions(String message) {
        List<Long> roles = new ArrayList<>();
        Matcher matcher = DiscordRegexPattern.ROLE_MENTION.matcher(message);
        while (matcher.find()) {
            roles.add(Long.parseLong(matcher.group()
                    .replaceFirst("<@&", "")
                    .replaceFirst(">", "")));
        }
        return roles;
    }

    /**
     * Parses the messages and returns the correct order of roles mentioned.
     *
     * @param api The Discord API to use.
     * @param message The message to parse.
     * @return A correctly ordered list of channel mentions.
     */
    public static Collection<Optional<Role>> getOrderedRoleMentions(DiscordApi api, String message) {
        return getOrderedRoleMentions(message).stream().map(api::getRoleById)
                .collect(Collectors.toList());
    }

    /**
     * Parses the messages and returns the correct order of channels mentioned.
     *
     * @param message The message to parse.
     * @return A correctly ordered list of channel mentions.
     */
    public static Collection<Long> getOrderedChannelMentions(String message) {
        List<Long> channels = new ArrayList<>();
        Matcher matcher = DiscordRegexPattern.CHANNEL_MENTION.matcher(message);
        while (matcher.find()) {
            channels.add(Long.parseLong(matcher.group()
                    .replaceFirst("<#", "")
                    .replaceFirst(">", "")));
        }
        return channels;
    }

    /**
     * Parses the messages and returns the correct order of channels mentioned.
     *
     * @param api The Discord API to use.
     * @param message The message to parse.
     * @return A correctly ordered list of channel mentions.
     */
    public static Collection<Optional<Channel>> getOrderedChannelMentions(DiscordApi api, String message) {
        return getOrderedChannelMentions(message).stream().map(api::getChannelById)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the name of command that the user potentially
     * wanted to type in. (mostly because of typos, etc).
     * <br>
     * This is useful for commands such as "help command"
     * where the user types in a wrong query and you can immediately
     * fill in with a suggested command.
     *
     * This method uses {@link Velen#getCommands()} as the participants
     * for the fuzzy scoring. (Please note that it may be a bit
     * expensive to use this).
     *
     * @param query The query to find.
     * @return the highest scored command.
     */
    public static String getCommandSuggestion(Velen velen, String query) {
        return internals.closest(query, velen.getCommands().stream().map(VelenCommand::getName)
                .collect(Collectors.toList())).getLeft();
    }

}
