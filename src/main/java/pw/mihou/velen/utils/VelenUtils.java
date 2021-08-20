package pw.mihou.velen.utils;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.DiscordRegexPattern;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.internals.VelenInternalUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class VelenUtils {

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
            users.add(Long.parseLong(matcher.group("id")));
        }
        return users;
    }

    /**
     * Parses the messages and returns the correct order of users mentioned.
     *
     * @param api     The Discord API to use.
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
            roles.add(Long.parseLong(matcher.group("id")));
        }
        return roles;
    }

    /**
     * Parses the messages and returns the correct order of roles mentioned.
     *
     * @param api     The Discord API to use.
     * @param message The message to parse.
     * @return A correctly ordered list of channel mentions.
     */
    public static Collection<Role> getOrderedRoleMentions(DiscordApi api, String message) {
        return getOrderedRoleMentions(message).stream().map(api::getRoleById)
                .map(role -> role.orElse(null))
                .filter(Objects::nonNull)
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
            channels.add(Long.parseLong(matcher.group("id")));
        }
        return channels;
    }

    /**
     * Parses the messages and returns the correct order of channels mentioned.
     *
     * @param api     The Discord API to use.
     * @param message The message to parse.
     * @return A correctly ordered list of channel mentions.
     */
    public static Collection<Channel> getOrderedChannelMentions(DiscordApi api, String message) {
        return getOrderedChannelMentions(message).stream().map(api::getChannelById)
                .map(channel -> channel.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the name of command that the user potentially
     * wanted to type in. (mostly because of typos, etc).
     * <br>
     * This is useful for commands such as "help command"
     * where the user types in a wrong query and you can immediately
     * fill in with a suggested command.
     * <p>
     * This method uses {@link Velen#getCommands()} as the participants
     * for the fuzzy scoring. (Please note that it may be a bit
     * expensive to use this).
     *
     * @param velen The velen instance to get all the commands from.
     * @param query The query to find.
     * @return the highest scored command.
     */
    public static String getCommandSuggestion(Velen velen, String query) {
        return internals.closest(query, velen.getCommands().stream().map(VelenCommand::getName)
                .collect(Collectors.toList())).getLeft();
    }

    /**
     * Checks if Role 1 is higher than Role 2 hierarchically.
     *
     * @param role1 The role to check.
     * @param role2 The role to compare with.
     * @return is Role 1 higher than Role 2?
     */
    public static boolean isRoleHigher(Role role1, Role role2) {
        return role1.getPosition() > role2.getPosition();
    }

    /**
     * Checks if the user has a higher role than the other user.
     *
     * @param user The user to check.
     * @param userToCompareAgainst The user to compare with.
     * @param server The server where the comparison should happen.
     * @return is User's Role higher than User?
     */
    public static boolean isUserRoleHigherThanUser(User user, User userToCompareAgainst, Server server) {
        return user.getRoles(server).stream()
                .anyMatch(r -> userToCompareAgainst.getRoles(server).stream()
                        .anyMatch(role -> isRoleHigher(r, role)));
    }

    /**
     * Checks if a string starts with the mention of a user/bot with a id.
     *
     * @param content The string to check.
     * @param mentionId The id of the user.
     * @return whether the content starts with the mention
     */
    public static boolean startsWithMention(String content, String mentionId) {
        return content.startsWith("<@") && (
                content.startsWith(String.format("<@%s>", mentionId))
                        || content.startsWith(String.format("<@!%s>", mentionId))
        );
    }

    /**
     * Splits a String with respect to escapes and double quotes.
     * This is internally used to get the args from a message.
     *
     * "1 2" 3 4 → 1 2, 3, 4
     * "1 \" 2 3" 4 → 1 2 " 3, 4
     * 1\ 2 3 4 → 1 2, 3, 4
     *
     * @param content The String to split.
     * @return The split String as Array.
     */
    public static String[] splitContent(String content) {
        // if string without " and \ just return the normal split
        if (!(content.contains("\"") || content.contains("\\"))) return content.split("\\s+");

        List<String> split = new ArrayList<>();

        boolean inDoubleQuotes = false;
        boolean currentCharEscaped = false;

        StringBuilder current = new StringBuilder();

        for (char ch : content.toCharArray()) {
            if (ch == '\\') {
                if (currentCharEscaped) {
                    current.append(ch); // append \ as it is escaped
                    currentCharEscaped = false;
                } else {
                    currentCharEscaped = true; // next char is escaped
                }
            } else {
                if (inDoubleQuotes) {
                    if (!currentCharEscaped && ch == '"') { // current char isn't escaped and a double quote
                        inDoubleQuotes = false; // leaves this double quote state
                    } else {
                        current.append(ch); // just apppend the char
                    }
                } else if (!currentCharEscaped // if not escaped
                        && Character.isWhitespace(ch)) { // if is white space
                    if (current.length() > 0) { // only add if there is something
                        split.add(current.toString());
                        current = new StringBuilder();
                    }
                } else if (!currentCharEscaped && ch == '"') {
                    // now in double qoutes
                    inDoubleQuotes = true;
                } else {
                    current.append(ch); // just apppend the char
                }

                if (currentCharEscaped) {
                    // this char was escaped; next isn't anymore
                    currentCharEscaped = false;
                }
            }
        }

        if (current.length() > 0) {
            // add remaining string to list
            split.add(current.toString());
        }

        return split.toArray(new String[0]);
    }

    /**
     * Parses an argument array into named args and an array of unnamed args.
     *
     * Examples:
     * "00 --arg --arg2=123 --arg3 456 789" → Pair({"arg": "", "arg2": "123", "arg3", "456"}, ["00", "789"])
     * "--arg 123 --arg=456" → Pair({"arg": "456"}, [])
     *
     * @param args The argument array to parse.
     * @return A Pair of a HashMap with the named args and a String[] with the unnamed args.
     */
    public static Pair<HashMap<String, String>, String[]> parseArgumentArgsArray(String[] args) {
        HashMap<String, String> namedArgs = new HashMap<>(); // {name: value, ...}
        List<String> normalArgs = new ArrayList<>(); // list of the unnamed args

        boolean nextIsValue = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                if (nextIsValue) {
                    // this can be used if it is used as a boolean flag
                    // "--arg --arg2=123" → {"arg": "", ...}
                    namedArgs.put(
                            args[i -1 ] // args[i -1 ] = the one before ("--arg" in the example)
                                    .substring(2) // substring(2) to remove the "--"
                            , "" // empty String to indicate that the arg is there but has no value
                    );

                    nextIsValue = false; // set it back to false
                }
                String[] argSplitOnEquals = arg.split("=", 2);
                if (argSplitOnEquals.length == 2) {
                    // "--arg=123" → {"arg": "123"}
                    namedArgs.put(
                            argSplitOnEquals[0] // name of the arg ("--arg" in the example)
                                    .substring(2), // substring(2) to remove the "--"
                            argSplitOnEquals[1] // the part after the equals ("123" in the example)
                    );
                } else {
                    nextIsValue = true;
                }
            } else if (nextIsValue) {
                // "--arg 123" → {"arg": "123"}
                namedArgs.put(
                        args[i -1 ]  // args[i -1 ] = the one before ("--arg" in the example)
                                .substring(2), // substring(2) to remove the "--"
                        arg // the current arg as the value ("123" in the example)
                );
                nextIsValue = false; // set it back to false
            } else {
                normalArgs.add(arg); // not a named arg, so add it to the list
            }
        }

        // last named arg wasn't added yet.
        if (nextIsValue) {
            // this can be used if it is used as a boolean flag
            // "--arg" → {"arg": ""}
            namedArgs.put(
                    args[args.length -1] //the last elemtent in the array. ("--arg" in the example)
                            .substring(2) // substring(2) to remove the "--"
                    , ""
            );
        }


        return new Pair<>(namedArgs, normalArgs.toArray(new String[0]));
    }

}
