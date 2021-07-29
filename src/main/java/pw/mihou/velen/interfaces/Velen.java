package pw.mihou.velen.interfaces;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;
import pw.mihou.velen.VelenBuilder;
import pw.mihou.velen.interfaces.messages.types.VelenPermissionMessage;
import pw.mihou.velen.interfaces.messages.types.VelenRatelimitMessage;
import pw.mihou.velen.interfaces.messages.types.VelenRoleMessage;
import pw.mihou.velen.internals.VelenBlacklist;
import pw.mihou.velen.prefix.VelenPrefixManager;
import pw.mihou.velen.ratelimiter.VelenRatelimiter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface Velen extends MessageCreateListener, SlashCommandCreateListener {

    /**
     * Creates a default Velen instance with default
     * settings, not recommended if you want to use per-server prefixes
     * and a different default cooldown, for those, use {@link VelenBuilder} or {@link Velen#builder()}.
     *
     * @return A default Valen.
     */
    static Velen ofDefaults() {
        return new VelenBuilder().build();
    }

    /**
     * Creates a new Velen Builder which you can use to build
     * a custom Velen.
     *
     * @return A Velen Builder.
     */
    static VelenBuilder builder() {
        return new VelenBuilder();
    }

    /**
     * Adds a command, we recommend using use {@link pw.mihou.velen.builders.VelenCommandBuilder} to build
     * the command instead of building it yourself.
     *
     * @param command The command to add, use {@link pw.mihou.velen.builders.VelenCommandBuilder} to build.
     * @return An updated Velen.
     */
    Velen addCommand(VelenCommand command);

    /**
     * Removes a command from Velen.
     *
     * @param command The command to remove.
     * @return An updated Velen.
     */
    Velen removeCommand(VelenCommand command);

    /**
     * Gets the current instance of Velen.
     *
     * @return The current instance of Velen.
     */
    Velen getInstance();

    /**
     * Gets the rate-limited message that is used whenever
     * a user is rate-limited (sent once every cycle).
     *
     * @return The rate-limited message.
     */
    VelenRatelimitMessage getRatelimitedMessage();

    /**
     * Gets the message sent whenever the user lacks
     * the permission to use the command.
     *
     * @return The no-permission message.
     */
    VelenPermissionMessage getNoPermissionMessage();

    /**
     * Gets the message sent whenever the user lacks
     * the role to use the command.
     *
     * @return The no-role message.
     */
    VelenRoleMessage getNoRoleMessage();

    /**
     * Gets the Velen Rate-limiter that is being used by the Velen.
     *
     * @return The Velen Rate-limiter.
     */
    VelenRatelimiter getRatelimiter();

    /**
     * Gets all the commands registered on the Velen.
     *
     * @return The commands registered.
     */
    List<VelenCommand> getCommands();

    /**
     * Gets all the commands registered with the specified category on the Velen.
     *
     * @param category The category to search for (case-sensitive).
     * @return The commands registered with the specified category.
     */
    List<VelenCommand> getCategory(String category);

    /**
     * Gets all the commands registered with the specified category on the Velen.
     *
     * @param category The category to search for (case-insensitive).
     * @return The commands registered with the specified category.
     */
    List<VelenCommand> getCategoryIgnoreCasing(String category);

    /**
     * Gets a certain command through its name.
     *
     * @param command The command to find.
     * @return An optional that possibly contains the command.
     */
    Optional<VelenCommand> getCommand(String command);

    /**
     * Gets all the categories of all commands in Velen.
     *
     * @return a Map with the category name
     * and immutable list of VelenCommand.
     */
    Map<String, List<VelenCommand>> getCategories();

    /**
     * Gets a certain command through its name (ignoring casing).
     * <br>
     * <b>This is deprecated since we are now using HashMaps to store commands, you can use {@link Velen#getCommand(String)}
     * instead for the same behavior.</b>
     * @param command The command to find.
     * @return An optional that possibly contains the command.
     */
    @Deprecated
    Optional<VelenCommand> getCommandIgnoreCasing(String command);

    /**
     * Registers all the slash commands registered under Velen.
     * This should only be done once unless you are changing the values
     * inside the commands (like the name of the command, etc).
     *
     * @param api The Discord API to register the commands to.
     * @return A CompletableFuture to mark its completion.
     */
    CompletableFuture<Void> registerAllSlashCommands(DiscordApi api);

    /**
     * Registers a specific slash command that is being utilized by
     * Velen, this is opposed to {@link Velen#registerAllSlashCommands(DiscordApi)} which
     * registers all the slash commands.
     * <br>
     * This will throw an {@link IllegalArgumentException} if the command is not
     * found.
     *
     * @param command The command to search for.
     * @param api The DiscordApi to use for registering the command.
     * @return The slash command returned.
     */
    CompletableFuture<SlashCommand> registerSlashCommand(String command, DiscordApi api);

    /**
     * Updates a specific slash command with the help of Velen, this can be used if
     * you want to keep the values that you made with {@link VelenCommand} but want to
     * update a specific part such as the description.
     * <br><br>
     * You can use {@link Velen#getAllSlashCommandIds(DiscordApi)} to retrieve the ID of the slash
     * command that you want to update.
     *
     * <br><br>
     * This also proxies to {@link Velen#updateSlashCommand(long, VelenCommand, DiscordApi)} after retrieving
     * the VelenCommand if it exists, otherwise throws an {@link IllegalArgumentException}.
     *
     * @param id The ID of the slash command that you want to update.
     * @param command The command to search.
     * @param api The DiscordApi to use for updating.
     * @return A new Slash Command instance.
     */
    CompletableFuture<SlashCommand> updateSlashCommand(long id, String command, DiscordApi api);

    /**
     * Updates a specific slash command with the help of Velen, this can be used if
     * you want to keep the values that you made with {@link VelenCommand} but want to
     * update a specific part such as the description.
     * <br><br>
     * You can use {@link Velen#getAllSlashCommandIds(DiscordApi)} to retrieve the ID of the slash
     * command that you want to update.
     *
     * @param id The ID of the slash command that you want to update.
     * @param command The command to search.
     * @param api The DiscordApi to use for updating.
     * @return A new Slash Command instance.
     */
    CompletableFuture<SlashCommand> updateSlashCommand(long id, VelenCommand command, DiscordApi api);

    /**
     * Retrieves all the IDs of the slash commands that are registered
     * under your Discord bot. This will return them in the format of [ID, Command Name]
     * which you can then use {@link Map#forEach(BiConsumer)} if you want to scroll through them.
     *
     * @param api The DiscordApi to use to retrieve the commands.
     * @return A map of slash command ids and name
     */
    CompletableFuture<Map<Long, String>> getAllSlashCommandIds(DiscordApi api);

    /**
     * This is used to check if the Velen instance currently
     * supports utilization of a blacklist (ignored user list).
     *
     * @return Does this Velen instance sport a blacklist?
     */
    boolean supportsBlacklist();

    /**
     * Gets the blacklist being used by this {@link Velen} instance.
     * It is recommended to use {@link Velen#supportsBlacklist()} first
     * before attempting to do so, unless you are confident that the blacklist
     * is supported.
     *
     * @return The blacklist instance that is being used.
     */
    VelenBlacklist getBlacklist();

    /**
     * Gets the Prefix Manager used by the Velen.
     *
     * @return The prefix manager used.
     */
    VelenPrefixManager getPrefixManager();

}
