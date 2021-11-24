package pw.mihou.velen.interfaces.routed;

import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.impl.VelenCommandImpl;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.internals.routing.VelenRoutedArgument;
import pw.mihou.velen.internals.routing.VelenUnderscoreParser;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class VelenRoutedOptions {

    private final VelenRoutedArgument[] arguments;
    private final MessageCreateEvent event;

    public VelenRoutedOptions(VelenCommand command, MessageCreateEvent event) {
        this.event = event;

        String[] commandIndexes = event.getMessageContent().split("\\s+");

        arguments = VelenUnderscoreParser.route(event.getMessageContent(), ((VelenCommandImpl)command).getFormats())
                .entrySet()
                .stream()
                .map(entry -> new VelenRoutedArgument(entry.getKey(), entry.getValue().getLeft(),
                        entry.getValue().getRight() == null ? commandIndexes[entry.getKey()] :  entry.getValue().getRight()))
                .toArray(VelenRoutedArgument[]::new);
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<String> withName(String name) {
        long duplicates = Arrays.stream(arguments).filter(arg -> arg.getName() != null && arg.getName().equalsIgnoreCase(name)).count();
        if (duplicates < 2) {
            return Arrays.stream(arguments)
                    .filter(arg -> arg.getName() != null && arg.getName().equalsIgnoreCase(name))
                    .map(VelenRoutedArgument::getValue)
                    .findFirst();
        } else {
            // Allow us to return all the values.
            return Optional.of(Arrays.stream(arguments)
                    .filter(arg -> arg.getName() != null && arg.getName().equalsIgnoreCase(name))
                    .map(VelenRoutedArgument::getValue)
                    .collect(Collectors.joining(" ")));
        }
    }

    /**
     * Internal method to clean up the non-numerical numbers in IDs
     * and transforms them properly.
     *
     * @param name The name of the argument.
     * @return The cleansed snowflake.
     */
    private Optional<Long> getNumberOnly(String name) {
        return withName(name).map(s -> s.replaceAll("[^\\d]", ""))
                .map(Long::parseLong);
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a long value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<Long> getLongWithName(String name) {
        return withName(name).map(Long::parseLong);
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a boolean value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<Boolean> getBooleanWithName(String name) {
        return withName(name).map(Boolean::parseBoolean);
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * an integer value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<Integer> getIntegerWithName(String name) {
        return withName(name).map(Integer::parseInt);
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a incoming webhook value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<CompletableFuture<IncomingWebhook>> getIncomingWebhookFromNamedArgument(String name) {
        return withName(name).map(s -> event.getApi().getIncomingWebhookByUrl(s));
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a channel value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<Channel> getChannelFromNamedArgument(String name) {
        return getNumberOnly(name)
                .flatMap(aLong -> event.getApi().getChannelById(aLong));
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a server channel value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<ServerChannel> getServerChannelFromNamedArgument(String name) {
        return getNumberOnly(name)
                .flatMap(aLong -> event.getApi().getServerChannelById(aLong));
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a text channel value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<TextChannel> getTextChannelFromNamedArgument(String name) {
        return getNumberOnly(name)
                .flatMap(aLong -> event.getApi().getTextChannelById(aLong));
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a server text channel value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<ServerTextChannel> getServerTextChannelFromNamedArgument(String name) {
        return getNumberOnly(name)
                .flatMap(aLong -> event.getApi().getServerTextChannelById(aLong));
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a voice channel value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<VoiceChannel> getVoiceChannelFromNamedArgument(String name) {
        return getNumberOnly(name)
                .flatMap(aLong -> event.getApi().getVoiceChannelById(aLong));
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a server voice channel value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<ServerVoiceChannel> getServerVoiceChannelFromNamedArgument(String name) {
        return getNumberOnly(name)
                .flatMap(aLong -> event.getApi().getServerVoiceChannelById(aLong));
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a emoji value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<KnownCustomEmoji> getEmojiFromNamedArgument(String name) {
        return getNumberOnly(name)
                .flatMap(aLong -> event.getApi().getCustomEmojiById(aLong));
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command; this also converts them to
     * a message value.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<CompletableFuture<Message>> getMessageFromNamedArgument(String name) {
        return withName(name).flatMap(s -> event.getApi().getMessageByLink(s));
    }

    /**
     * Retrieves the user from the named argument, this only works if you have
     * user cache enabled and the user is cached, otherwise please use {@link VelenRoutedOptions#requestUserFromNamedArgument(String name)}.
     *
     * @param name The name of the argument to fetch.
     * @return The user instance if present in cached.
     */
    public Optional<User> getUserFromNamedArgument(String name) {
        return getNumberOnly(name)
                .flatMap(aLong -> event.getApi().getCachedUserById(aLong));
    }

    /**
     * Retrieves the user from the named argument, this first fetches from cache if it is present,
     * otherwise it will go request the user data from Discord.
     *
     * @param name The name of the argument to fetch.
     * @return The user instance if present in cached.
     */
    public Optional<CompletableFuture<User>> requestUserFromNamedArgument(String name) {
        if (!getNumberOnly(name).isPresent()) {
            return Optional.empty();
        }

        return Optional.of(CompletableFuture.supplyAsync(() -> getUserFromNamedArgument(name)
                .orElse(event.getApi().getUserById(getNumberOnly(name).get()).join())));
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command.
     *
     * @param index The index of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<String> withIndex(int index) {
        return Arrays.stream(arguments)
                .filter(arg -> arg.getIndex() == index)
                .map(VelenRoutedArgument::getValue)
                .findFirst();
    }

}
