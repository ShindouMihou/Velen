package pw.mihou.velen.utils;

import org.javacord.api.util.DiscordRegexPattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

public class ValenUtils {

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

}
