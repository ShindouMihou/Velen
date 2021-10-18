package pw.mihou.velen.internals.routing.routers;

import org.javacord.api.util.DiscordRegexPattern;
import pw.mihou.velen.internals.routing.VelenUnderscoreRoute;
import pw.mihou.velen.utils.Pair;

import static pw.mihou.velen.internals.routing.VelenUnderscoreParser.cleanseParameterType;
import static pw.mihou.velen.internals.routing.VelenUnderscoreParser.hasParameterType;

public class OfTypeRouter implements VelenUnderscoreRoute {

    @Override
    public Pair<Boolean, String> accept(String s, String name, int index, String[] commandIndexes,
                                        String messageArgument, String command, String format) {
        if (hasParameterType(name, "user")) {
            return Pair.of(DiscordRegexPattern.USER_MENTION.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "user"));
        }

        if (hasParameterType(name, "channel")) {
            return Pair.of(DiscordRegexPattern.CHANNEL_MENTION.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "channel"));
        }

        if (hasParameterType(name, "role")) {
            return Pair.of(DiscordRegexPattern.ROLE_MENTION.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "role"));
        }

        if (hasParameterType(name, "message")) {
            return Pair.of(DiscordRegexPattern.MESSAGE_LINK.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "message"));
        }

        if (hasParameterType(name, "emoji")) {
            return Pair.of(DiscordRegexPattern.CUSTOM_EMOJI.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "emoji"));
        }

        if (hasParameterType(name, "webhook")) {
            return Pair.of(DiscordRegexPattern.WEBHOOK_URL.matcher(messageArgument).matches(),
                    cleanseParameterType(name, "webhook"));
        }

        if (hasParameterType(name, "boolean")) {
            return Pair.of(messageArgument.equalsIgnoreCase("true")
                            || messageArgument.equalsIgnoreCase("false"),
                    cleanseParameterType(name, "boolean"));
        }

        if (hasParameterType(name, "numeric")) {
            return Pair.of(messageArgument.chars().allMatch(Character::isDigit),
                    cleanseParameterType(name, "numeric"));
        }

        return Pair.of(true, name);
    }

}
