package pw.mihou.velen.interfaces;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.DiscordRegexPattern;
import pw.mihou.velen.utils.VelenUtils;

import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VelenCommandArguments implements VelenArguments {
    private final String[] arguments;
    private final HashMap<String, String> namedArguments;
    private final HashMap<String, String> namedArgumentsWithLowerCaseKey;
    private final DiscordApi api;

    VelenCommandArguments(String[] args, DiscordApi api) {
        this.arguments = args;
        namedArguments = VelenUtils.parseArgumentArgsArray(args).getLeft();
        namedArgumentsWithLowerCaseKey = VelenUtils.parseArgumentArgsArray(args, true).getLeft();
        this.api = api;
    }

    private boolean stringToBoolean(String str) {
        if (str.isEmpty()) { // if string is empty it is a flag like --help so it should return true
            return true;
        } else {
            return Boolean.parseBoolean(str);
        }
    }

    @Override
    public Optional<Boolean> getBooleanOptionWithName(String name) {
        return getStringOptionWithName(name).map(this::stringToBoolean);

    }

    @Override
    public Optional<Boolean> getBooleanOptionWithNameIgnoreCasing(String name) {
        return getStringOptionWithNameIgnoreCasing(name).map(this::stringToBoolean);
    }

    @Override
    public Optional<String> getStringOptionWithName(String name) {
        return namedArguments.containsKey(name)  // check if map contains key
                ? Optional.of(namedArguments.get(name))  // get value
                : Optional.empty(); // map doesn't contain key, so return empty Optional.
    }

    @Override
    public Optional<String> getStringOptionWithNameIgnoreCasing(String name) {
        String key = name.toLowerCase();
        return namedArgumentsWithLowerCaseKey.containsKey(key)  // check if map contains key
                ? Optional.of(namedArgumentsWithLowerCaseKey.get(key))  // get value
                : Optional.empty(); // map doesn't contain key, so return empty Optional.
    }

    @Override
    public Optional<Integer> getIntegerOptionWithName(String name) {
        return getStringOptionWithName(name).map(Integer::parseInt);
    }

    @Override
    public Optional<Integer> getIntegerOptionWithNameIgnoreCasing(String name) {
        return getStringOptionWithNameIgnoreCasing(name).map(Integer::parseInt);
    }

    @Override
    public Optional<ServerChannel> getChannelOptionWithName(String name) {
        return getStringOptionWithName(name)
                .flatMap(str -> getIdFromMention(str, DiscordRegexPattern.CHANNEL_MENTION))
                .flatMap(api::getServerChannelById);
    }

    @Override
    public Optional<ServerChannel> getChannelOptionWithNameIgnoreCasing(String name) {
        return getStringOptionWithNameIgnoreCasing(name)
                .flatMap(str -> getIdFromMention(str, DiscordRegexPattern.CHANNEL_MENTION))
                .flatMap(api::getServerChannelById);
    }

    @Override
    public Optional<User> getUserOptionWithName(String name) {
        return getStringOptionWithName(name)
                .flatMap(str -> getIdFromMention(str, DiscordRegexPattern.USER_MENTION))
                .flatMap(api::getCachedUserById);
    }

    @Override
    public Optional<User> getUserOptionWithNameIgnoreCasing(String name) {
        return getStringOptionWithNameIgnoreCasing(name)
                .flatMap(str -> getIdFromMention(str, DiscordRegexPattern.USER_MENTION))
                .flatMap(api::getCachedUserById);
    }

    @Override
    public Optional<Role> getRoleOptionWithName(String name) {
        return getStringOptionWithName(name)
                .flatMap(str -> getIdFromMention(str, DiscordRegexPattern.ROLE_MENTION))
                .flatMap(api::getRoleById);
    }

    @Override
    public Optional<Role> getRoleOptionWithNameIgnoreCasing(String name) {
        return getStringOptionWithNameIgnoreCasing(name)
                .flatMap(str -> getIdFromMention(str, DiscordRegexPattern.ROLE_MENTION))
                .flatMap(api::getRoleById);
    }

    @Override
    public Optional<Mentionable> getMentionableOptionWithName(String name) {
        Optional<Mentionable> optional = getRoleOptionWithName(name).map(Mentionable.class::cast);
        if (optional.isPresent()) {
            return optional;
        }

        optional = getChannelOptionWithName(name).map(Mentionable.class::cast);
        if (optional.isPresent()) {
            return optional;
        }

        return getUserOptionWithName(name).map(Mentionable.class::cast);
    }

    @Override
    public Optional<Mentionable> getMentionableOptionWithNameIgnoreCasing(String name) {
        Optional<Mentionable> optional = getRoleOptionWithNameIgnoreCasing(name).map(Mentionable.class::cast);
        if (optional.isPresent()) {
            return optional;
        }

        optional = getChannelOptionWithNameIgnoreCasing(name).map(Mentionable.class::cast);
        if (optional.isPresent()) {
            return optional;
        }

        return getUserOptionWithNameIgnoreCasing(name).map(Mentionable.class::cast);
    }

    @Override
    public Optional<Boolean> getBooleanOptionWithIndex(int index) {
        return getStringOptionWithIndex(index).map(Boolean::parseBoolean);
    }

    @Override
    public Optional<String> getStringOptionWithIndex(int index) {
        return  index >= arguments.length
                ? Optional.empty()
                : Optional.of(arguments[index]);
    }

    @Override
    public Optional<Integer> getIntegerOptionWithIndex(int index) {
        return getStringOptionWithIndex(index).map(Integer::parseInt);
    }

    @Override
    public Optional<ServerChannel> getChannelOptionWithIndex(int index) {
        return getStringOptionWithIndex(index)
                .flatMap(str -> getIdFromMention(str, DiscordRegexPattern.CHANNEL_MENTION))
                .flatMap(api::getServerChannelById);
    }

    @Override
    public Optional<User> getUserOptionWithIndex(int index) {
        return getStringOptionWithIndex(index)
                .flatMap(str -> getIdFromMention(str, DiscordRegexPattern.USER_MENTION))
                .flatMap(api::getCachedUserById);
    }

    @Override
    public Optional<Role> getRoleOptionWithIndex(int index) {
        return getStringOptionWithIndex(index)
                .flatMap(str -> getIdFromMention(str, DiscordRegexPattern.USER_MENTION))
                .flatMap(api::getRoleById);
    }

    @Override
    public Optional<Mentionable> getMentionableOptionWithIndex(int index) {
        Optional<Mentionable> optional = getRoleOptionWithIndex(index).map(Mentionable.class::cast);
        if (optional.isPresent()) {
            return optional;
        }

        optional = getChannelOptionWithIndex(index).map(Mentionable.class::cast);
        if (optional.isPresent()) {
            return optional;
        }

        return getUserOptionWithIndex(index).map(Mentionable.class::cast);
    }

    private Optional<String> getIdFromMention(String str, Pattern mentionPattern) {
        Matcher mention = mentionPattern.matcher(str);
        if (!mention.matches()) {
            // maybe it is just the id and not a mention
            return getAsSnowflake(str);
        }
        // get the id from the mention
        return Optional.ofNullable(mention.group("id"));
    }

    private Optional<String> getAsSnowflake(String str) {
        if (DiscordRegexPattern.SNOWFLAKE.matcher(str).matches()) {
            return Optional.of(str);
        } else {
            return Optional.empty();
        }
    }
}
