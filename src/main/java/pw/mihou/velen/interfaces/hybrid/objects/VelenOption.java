package pw.mihou.velen.interfaces.hybrid.objects;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.util.DiscordRegexPattern;
import pw.mihou.velen.interfaces.hybrid.objects.subcommands.VelenSubcommand;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

public class VelenOption {

    private final String arg;
    private final SlashCommandInteractionOption option;
    private final DiscordApi api;
    private final VelenHybridArguments arguments;
    private final int index;

    private VelenOption(int index, String arg, SlashCommandInteractionOption option, DiscordApi api,
                        VelenHybridArguments arguments) {
        this.index = index;
        this.arg = arg;
        this.option = option;
        this.api = api;
        this.arguments = arguments;
    }

    /**
     * Creates a new Velen Option that originated from a message command.
     *
     * @param arg The argument vaule.
     */
    public VelenOption(int index, String arg, DiscordApi api, VelenHybridArguments arguments) {
        this(index, arg, null, api, arguments);
    }

    /**
     * Creates a new Velen Option that originated from a Slash Command.
     *
     * @param option The option value.
     */
    public VelenOption(int index, SlashCommandInteractionOption option, VelenHybridArguments arguments) {
        this(index, null, option, null, arguments);
    }

    /**
     * Retrieves the value of this Option as a String.
     *
     * @return The string value if present.
     */
    public Optional<String> asString() {
        if(arg == null)
            return option.getStringValue();

        return Optional.of(arg);
    }

    /**
     * Retrieves the value of this Option as a Boolean.
     *
     * @return The Boolean value if present.
     */
    public Optional<Boolean> asBoolean() {
        if(arg == null)
            return option.getBooleanValue();

        return Optional.of(Boolean.parseBoolean(arg));
    }

    /**
     * Retrieves the value of this Option as an Integer.
     *
     * @return The Integer value if present.
     */
    public Optional<Integer> asInteger() {
        if(arg == null)
            return option.getIntValue();

        return Optional.of(Integer.parseInt(arg));
    }

    /**
     * Retrieves the value of this Option as an Long.
     *
     * @return The Long value if present.
     */
    public Optional<Long> asLong() {
        if(arg == null)
            return option.getStringValue().map(Long::parseLong);

        return Optional.of(arg).map(Long::parseLong);
    }

    /**
     * Retrieves the value of this Option as a User, not to be confused by {@link VelenOption#asUser()}
     * which does not request the user if not present.
     *
     * @return The User value if present in the form of a CompletableFuture.
     */
    public Optional<CompletableFuture<User>> requestUser() {
        if(arg == null)
            // Reminder, this actually fetches from cache if it is there.
            return option.requestUserValue();

        Matcher matcher = DiscordRegexPattern.USER_MENTION.matcher(arg);
        if(!matcher.matches())
            return Optional.empty();

        return Optional.of(matcher.group("id"))
                .map(Long::parseLong)
                .map(aLong -> Objects.requireNonNull(api).getUserById(aLong));
    }

    /**
     * Retrieves the value of this Option as a User, not to be confused
     * by {@link VelenOption#requestUser()} which requests the user if not present.
     *
     * @return The User value if present.
     */
    public Optional<User> asUser() {
        if(arg == null)
            return option.getUserValue();

        Matcher matcher = DiscordRegexPattern.USER_MENTION.matcher(arg);
        if(!matcher.matches())
            return Optional.empty();

        // Follow the behavior of Javacord closely.
        return Objects.requireNonNull(api).getCachedUserById(Long.parseLong(matcher.group("id")));
    }

    /**
     * Retrieves the value of this Option as a Channel.
     *
     * @return The Channel value if present.
     */
    public Optional<Channel> asChannel() {
        if(arg == null)
            // How does this work, I don't know but Java approves it.
            return option.getChannelValue().map(serverChannel -> serverChannel);

        Matcher matcher = DiscordRegexPattern.CHANNEL_MENTION.matcher(arg);
        if(!matcher.matches())
            return Optional.empty();

        return Objects.requireNonNull(api).getChannelById(Long.parseLong(matcher.group("id")));
    }

    /**
     * Retrieves the value of this Option as a Role.
     *
     * @return The Role value if present.
     */
    public Optional<Role> asRole() {
        if(arg == null)
            return option.getRoleValue();

        Matcher matcher = DiscordRegexPattern.CHANNEL_MENTION.matcher(arg);
        if(!matcher.matches())
            return Optional.empty();

        return Objects.requireNonNull(api).getRoleById(Long.parseLong(matcher.group("id")));
    }

    /**
     * Retrieves the value of this Option as a Mentionable, this could be a {@link Channel},
     * {@link Role} or {@link User}, not to be confused by {@link VelenOption#requestMentionable()} which
     * requests the user if not present.
     *
     * @return The Mentionable value if present.
     */
    public Optional<Mentionable> asMentionable() {
        if(arg == null)
            return option.getMentionableValue();

        Optional<Role> isRole = asRole();
        if(isRole.isPresent())
            return isRole.map(role -> role);

        Optional<Channel> isChannel = asChannel();
        if(isChannel.isPresent())
            return isChannel.map(Mentionable.class::cast);

        Optional<User> isUser = asUser();
        if(isUser.isPresent())
            return isUser.map(user -> user);

        return Optional.empty();
    }

    /**
     * Retrieves the value of this Option as a Mentionable, this could be a {@link Channel},
     * {@link Role} or {@link User}, not to be confused by {@link VelenOption#asMentionable()} ()} which
     * does not the user if not present.
     *
     * @return The Mentionable value if present.
     */
    public Optional<CompletableFuture<Mentionable>> requestMentionable() {
        Optional<Mentionable> optionalMentionable = asMentionable();

        if(optionalMentionable.isPresent())
            return optionalMentionable.map(CompletableFuture::completedFuture);

        return requestUser().map(future -> future.thenApply(Mentionable.class::cast));
    }

    /**
     * Retrieves all the options available, this is a proxy of {@link VelenHybridArguments#getOptions()}.
     *
     * @return The arguments to fetch.
     */
    public VelenOption[] getOptions() {
        return arguments.getOptions();
    }

    /**
     * Retrieves this option as a subcommand.
     *
     * @return A {@link VelenSubcommand} instance containing all its options.
     */
    public VelenSubcommand asSubcommand() {
        return new VelenSubcommand(
                arg == null ? Objects.requireNonNull(option).getName() : arg,
                getIndex(),
                api,
                option != null ? option.isSubcommandOrGroup() ? option.getOptions() : null : null,
                arguments.asMessageOptions().orElse(null),
                arguments);
    }

    /**
     * Retrieves the index of this option.
     *
     * @return The index of this option.
     */
    public int getIndex() {
        return index;
    }
}
