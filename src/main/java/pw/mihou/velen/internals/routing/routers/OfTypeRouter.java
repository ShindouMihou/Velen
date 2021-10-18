package pw.mihou.velen.internals.routing.routers;

import org.javacord.api.util.DiscordRegexPattern;
import pw.mihou.velen.internals.routing.VelenUnderscoreRoute;
import pw.mihou.velen.utils.Pair;

import static pw.mihou.velen.internals.routing.VelenUnderscoreParser.*;

public class OfTypeRouter implements VelenUnderscoreRoute {

    @Override
    public Pair<Boolean, String> accept(String s, String name, int index, String[] commandIndexes,
                                        String messageArgument, String command, String format) {
        // What a beautiful if-else hell.
        if (hasParameterType(name, "user")) {
            return Pair.of(DiscordRegexPattern.USER_MENTION.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "user"));
        } else if (hasParameterType(name, "channel")) {
            return Pair.of(DiscordRegexPattern.CHANNEL_MENTION.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "channel"));
        } else if (hasParameterType(name, "role")) {
            return Pair.of(DiscordRegexPattern.ROLE_MENTION.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "role"));
        } else if (hasParameterType(name, "message")) {
            return Pair.of(DiscordRegexPattern.MESSAGE_LINK.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "message"));
        } else if (hasParameterType(name, "emoji")) {
            return Pair.of(DiscordRegexPattern.CUSTOM_EMOJI.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "emoji"));
        } else if (hasParameterType(name, "webhook")) {
            return Pair.of(DiscordRegexPattern.WEBHOOK_URL.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "webhook"));
        } else if (hasParameterType(name, "boolean")) {
            return Pair.of(messageArgument.equalsIgnoreCase("true")
                            || messageArgument.equalsIgnoreCase("false"),
                    cleanseParameterType(name, "boolean"));
        } else if (hasParameterType(name, "numeric")) {
            return Pair.of(messageArgument.chars().allMatch(Character::isDigit),
                    cleanseParameterType(name, "numeric"));
        } else if (hasParameterType(name, "string")) {
            return Pair.of(true, cleanseParameterType(name, "string"));
        } else if (hasParameterType(name, "integer")) {
            return Pair.of(messageArgument.chars().allMatch(Character::isDigit),
                    cleanseParameterType(name, "integer"));
        } else {
            name = name.replaceFirst(collect(":of(", ')', name), "");
        }

        return Pair.of(true, name);
    }

}
