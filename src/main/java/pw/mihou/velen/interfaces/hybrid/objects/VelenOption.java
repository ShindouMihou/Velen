package pw.mihou.velen.interfaces.hybrid.objects;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.util.DiscordRegexPattern;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.hybrid.objects.subcommands.VelenSubcommand;
import pw.mihou.velen.internals.routing.VelenRoutedArgument;

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
    private final String name;
    private final VelenCommand vl;

    /**
     * Creates a new {@link VelenOption} that can be used to alternatively select
     * different options from an event without worrying about whether it is a message or
     * slash command.
     *
     * @param index The index number or position of the argument.
     * @param arg The message routed argument.
     * @param option The slash command option.
     * @param api The Discord API shard received.
     * @param arguments The arguments for hybrid.
     * @param vl The command that is in charge of this.
     */
    private VelenOption(int index, VelenRoutedArgument arg, SlashCommandInteractionOption option, DiscordApi api,
                        VelenHybridArguments arguments, VelenCommand vl) {
        this.index = index;
        if (arg != null)
            this.arg = arg.getValue();
        else
            this.arg = null;

        this.option = option;
        this.api = api;
        this.arguments = arguments;
        this.vl = vl;

        if (option != null) {
            this.name = option.getName();
        } else {
            if (arg != null && arg.getName() != null)
                this.name = arg.getName();
            else
                this.name = null;
        }
    }

    /**
     * Retrieves the name of this argument, this can return as null if the
     * command does not have any sort of format setup.
     *
     * @return The name of this argument | null.
     */
    public String getName() {
        return name;
    }

    /**
     * Creates a new Velen Option that originated from a message command.
     *
     * @param arg The argument value.
     * @param vl The Velen Command that is in charge of this.
     * @param api The Discord API shard that the event came from.
     * @param arguments The hybrid command arguments.
     * @param index The index or position number of the argument.
     */
    public VelenOption(int index, VelenRoutedArgument arg, DiscordApi api, VelenHybridArguments arguments, VelenCommand vl) {
        this(index, arg, null, api, arguments, vl);
    }

    /**
     * Creates a new Velen Option that originated from a Slash Command.
     *
     * @param option The option value.
     */
    public VelenOption(int index, SlashCommandInteractionOption option, VelenHybridArguments arguments, VelenCommand vl) {
        this(index, null, option, null, arguments, vl);
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
            return option.getLongValue().map(Long::intValue);

        return Optional.of(Integer.parseInt(arg));
    }

    /**
     * Retrieves the value of this Option as an Long.
     *
     * @return The Long value if present.
     */
    public Optional<Long> asLong() {
        if(arg == null)
            return option.getLongValue();

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

        Matcher matcher = DiscordRegexPattern.ROLE_MENTION.matcher(arg);
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
                arguments.asRoutedArguments().orElse(null),
                arguments, vl);
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
