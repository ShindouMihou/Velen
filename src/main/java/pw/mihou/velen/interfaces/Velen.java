package pw.mihou.velen.interfaces;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.velen.VelenBuilder;
import pw.mihou.velen.builders.VelenCategoryBuilder;
import pw.mihou.velen.interfaces.afterware.VelenAfterware;
import pw.mihou.velen.interfaces.afterware.types.VelenHybridAfterware;
import pw.mihou.velen.interfaces.afterware.types.VelenMessageAfterware;
import pw.mihou.velen.interfaces.afterware.types.VelenSlashAfterware;
import pw.mihou.velen.interfaces.extensions.VelenCompany;
import pw.mihou.velen.interfaces.messages.types.VelenPermissionMessage;
import pw.mihou.velen.interfaces.messages.types.VelenRatelimitMessage;
import pw.mihou.velen.interfaces.messages.types.VelenRoleMessage;
import pw.mihou.velen.interfaces.middleware.VelenMiddleware;
import pw.mihou.velen.interfaces.middleware.types.VelenHybridMiddleware;
import pw.mihou.velen.interfaces.middleware.types.VelenMessageMiddleware;
import pw.mihou.velen.interfaces.middleware.types.VelenSlashMiddleware;
import pw.mihou.velen.internals.VelenBlacklist;
import pw.mihou.velen.prefix.VelenPrefixManager;
import pw.mihou.velen.ratelimiter.VelenRatelimiter;
import pw.mihou.velen.utils.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public interface Velen extends MessageCreateListener, SlashCommandCreateListener, VelenCompany {

    /**
     * Creates a default Velen instance with default
     * settings, not recommended if you want to use per-server prefixes
     * and a different default cooldown, for those, use {@link VelenBuilder} or {@link Velen#builder()}.
     *
     * @return A default Velen.
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
     * Loads all commands from a specific directory.
     *
     * @param directory The directory to search for.
     * @return The {@link Velen}  instance for chain-calling methods..
     */
    Velen loadFrom(String directory);

    /**
     * Loads all commands from a specific directory.
     *
     * @param directory The directory to search for.
     * @return The {@link Velen}  instance for chain-calling methods..
     */
    Velen loadFrom(File directory);

    /**
     * Loads all commands specified.
     *
     * @param files The files to load.
     * @return The {@link Velen}  instance for chain-calling methods..
     */
    Velen load(File... files);

    /**
     * Retrieves the afterware with the specified name.
     *
     * @param name The name of the afterware to fetch.
     * @return The afterware if present.
     */
    Optional<VelenAfterware> getAfterware(String name);

    /**
     * Stores the afterware inside this instance, it is not generally recommended to
     * use this method but instead use the other three similar methods which are specific
     * to a type of command.
     *
     * @param name The name of the middleware.
     * @param afterware The afterware to store.
     * @return The Velen instance for chain-calling methods.
     */
    Velen storeAfterware(String name, VelenAfterware afterware);

    /**
     * Adds a hybrid command afterware to the instance which can then be used
     * to retrieve later.
     *
     * @param name The name of the afterware.
     * @param afterware The afterware itself.
     * @return The Velen instance for chain-calling methods.
     */
    default Velen addHybridAfterware(String name, VelenHybridAfterware afterware) {
        return storeAfterware(name, afterware);
    }

    /**
     * Adds a message command afterware to the instance which can then be used
     * to retrieve later.
     *
     * @param name The name of the afterware.
     * @param afterware The afterware itself.
     * @return The Velen instance for chain-calling methods.
     */
    default Velen addMessageAfterware(String name, VelenMessageAfterware afterware) {
        return storeAfterware(name, afterware);
    }

    /**
     * Adds a slash command afterware to the instance which can then be used
     * to retrieve later.
     *
     * @param name The name of the afterware.
     * @param afterware The afterware itself.
     * @return The Velen instance for chain-calling methods.
     */
    default Velen addSlashAfterware(String name, VelenSlashAfterware afterware) {
        return storeAfterware(name, afterware);
    }

    /**
     * Retrieves the middleware with the specified name.
     *
     * @param name The name of the middleware to fetch.
     * @return The middleware if present.
     */
    Optional<VelenMiddleware> getMiddleware(String name);

    /**
     * Stores the middleware inside this instance, it is not generally recommended to
     * use this method but instead use the other three similar methods which are specific
     * to a type of command.
     *
     * @param name The name of the middleware.
     * @param middleware The middleware to store.
     * @return The Velen instance for chain-calling methods.
     */
    Velen storeMiddleware(String name, VelenMiddleware middleware);

    /**
     * Adds a hybrid command middleware to the instance which can then be used
     * to retrieve later.
     *
     * @param name The name of the middleware.
     * @param middleware The middleware itself.
     * @return The Velen instance for chain-calling methods.
     */
    default Velen addHybridMiddleware(String name, VelenHybridMiddleware middleware) {
        return storeMiddleware(name, middleware);
    }

    /**
     * Adds a message command middleware to the instance which can then be used
     * to retrieve later.
     *
     * @param name The name of the middleware.
     * @param middleware The middleware itself.
     * @return The Velen instance for chain-calling methods.
     */
    default Velen addMessageMiddleware(String name, VelenMessageMiddleware middleware) {
        return storeMiddleware(name, middleware);
    }

    /**
     * Adds a slash command middleware to the instance which can then be used
     * to retrieve later.
     *
     * @param name The name of the middleware.
     * @param middleware The middleware itself.
     * @return The Velen instance for chain-calling methods.
     */
    default Velen addSlashMiddleware(String name, VelenSlashMiddleware middleware) {
        return storeMiddleware(name, middleware);
    }

    /**
     * Adds a new handler for message commands.
     * @param name The name of the handler, it must be unique otherwise it would collide.
     * @param handler The handler to add.
     * @return The {@link Velen}  instance for chain-calling methods..
     */
    Velen addHandler(String name, VelenEvent handler);

    /**
     * Adds a new handler for slash commands.
     * @param name The name of the handler, it must be unique otherwise it would collide.
     * @param handler The handler to add.
     * @return The {@link Velen}  instance for chain-calling methods..
     */
    Velen addHandler(String name, VelenSlashEvent handler);

    /**
     * Adds a new handler for hybrid commands.
     * @param name The name of the handler, it must be unique otherwise it would collide.
     * @param handler The handler to add.
     * @return The {@link Velen}  instance for chain-calling methods..
     */
    Velen addHandler(String name, VelenHybridHandler handler);

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
     * Gets all the commands registered with the specified category on the Velen.
     *
     * @param category The category to search for (case-sensitive).
     * @return The commands registered with the specified category.
     */
    default List<VelenCommand> getCategory(String category) {
        return getCommands().stream()
                .filter(velenCommand -> velenCommand.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the category instance with the specified name.
     *
     * @param category The category to search for.
     * @return The {@link VelenCategory} instance.
     */
    default VelenCategory findCategory(String category) {
        return findCategories().get(category.toLowerCase());
    }

    /**
     * Retrieves the categories that are stored inside this Velen instance.
     *
     * @return All the stored {@link VelenCategory} instances.
     */
    Map<String, VelenCategory> findCategories();

    /**
     * Stores a specific category into the {@link Velen} instance.
     *
     * @param builder The builder used to create the category.
     * @return The {@link Velen} instance for chain-calling methods.
     */
    default Velen addCategory(VelenCategoryBuilder builder) {
        return addCategory(builder.create(this));
    }

    /**
     * Stores a specific category into the {@link Velen} instance.
     *
     * @param category The category to store.
     * @return The {@link Velen} instance for chain-calling methods.
     */
    Velen addCategory(VelenCategory category);

    /**
     * Gets all the commands registered with the specified category on the Velen.
     *
     * @param category The category to search for (case-insensitive).
     * @return The commands registered with the specified category.
     */
    default List<VelenCommand> getCategoryIgnoreCasing(String category)  {
        return getCommands().stream()
                .filter(velenCommand -> velenCommand.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Gets all the categories of all commands in Velen.
     *
     * @return a Map with the category name
     * and immutable list of VelenCommand.
     */
    default Map<String, List<VelenCommand>> getCategories() {
        Map<String, List<VelenCommand>> catMap = new HashMap<>();
        getCommands()
                .stream()
                .filter(command -> !command.getCategory().isEmpty())
                .forEach(velenCommand -> {
                    if(!catMap.containsKey(velenCommand.getCategory()))
                        catMap.put(velenCommand.getCategory(), new ArrayList<>());

                    catMap.get(velenCommand.getCategory()).add(velenCommand);
                });

        // We want the list to be returned as an immutable list.
        return catMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> Collections.unmodifiableList(e.getValue())));
    }

    /**
     * Registers all the slash commands registered under Velen.
     * This should only be done once unless you are changing the values
     * inside the commands (like the name of the command, etc).
     *
     * <br><br><b>We recommend using {@link pw.mihou.velen.internals.observer.VelenObserver} to
     * automate registration and updating of slash commands.</b>
     * @param api The Discord API to register the commands to.
     * @return A CompletableFuture to mark its completion.
     */
    default CompletableFuture<Void> registerAllSlashCommands(DiscordApi api)  {
        return CompletableFuture.allOf(getCommands().stream().filter(VelenCommand::supportsSlashCommand)
                .map(velenCommand -> {
                    Pair<Long, SlashCommandBuilder> pair = velenCommand.asSlashCommand();

                    if (pair.getLeft() != null && pair.getLeft() != 0L) {
                        Optional<Server> server = api.getServerById(pair.getLeft());
                        if (server.isPresent()) {
                            return pair.getRight().createForServer(server.get());
                        } else {
                            throw new IllegalArgumentException("Server " + pair.getLeft() + " couldn't be found for " +
                                    "slash command: " + pair.getRight().toString());
                        }
                    }

                    return pair.getRight().createGlobal(api);
                }).toArray(CompletableFuture[]::new)).exceptionally(ExceptionLogger.get());
    }

    /**
     * Registers a specific slash command that is being utilized by
     * Velen, this is opposed to {@link Velen#registerAllSlashCommands(DiscordApi)} which
     * registers all the slash commands.
     * <br>
     * This will throw an {@link IllegalArgumentException} if the command is not
     * found.
     *
     * <br><br><b>We recommend using {@link pw.mihou.velen.internals.observer.VelenObserver} to
     * automate registration and updating of slash commands.</b>
     * @param command The command to search for.
     * @param api The DiscordApi to use for registering the command.
     * @return The slash command returned.
     */
    default CompletableFuture<SlashCommand> registerSlashCommand(String command, DiscordApi api) {
        Optional<VelenCommand> optional = getCommand(command);
        if (!optional.isPresent())
            throw new IllegalArgumentException("The command " + command + " couldn't be found!");

        VelenCommand c = optional.get();
        if (!c.supportsSlashCommand())
            throw new IllegalArgumentException("The command " + command + " does not support slash commands!");

        Pair<Long, SlashCommandBuilder> pair = c.asSlashCommand();
        if (pair.getLeft() != null && pair.getLeft() != 0L) {
            Optional<Server> server = api.getServerById(pair.getLeft());
            if (server.isPresent()) {
                return pair.getRight().createForServer(server.get());
            } else {
                throw new IllegalArgumentException("Server " + pair.getLeft() + " couldn't be found for " +
                        "slash command: " + pair.getRight().toString());
            }
        }

        return pair.getRight().createGlobal(api);
    }

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
     * <br><br><b>We recommend using {@link pw.mihou.velen.internals.observer.VelenObserver} to
     * automate registration and updating of slash commands.</b>
     * @param id The ID of the slash command that you want to update.
     * @param command The command to search.
     * @param api The DiscordApi to use for updating.
     * @return A new Slash Command instance.
     */
    default CompletableFuture<SlashCommand> updateSlashCommand(long id, String command, DiscordApi api) {
        Optional<VelenCommand> optional = getCommand(command);
        if (!optional.isPresent())
            throw new IllegalArgumentException("The command " + command + " couldn't be found!");

        VelenCommand c = optional.get();
        if(!c.supportsSlashCommand())
            throw new IllegalArgumentException("The command " + command + " does not support slash commands!");

        return updateSlashCommand(id, c, api);
    }

    /**
     * Updates a specific slash command with the help of Velen, this can be used if
     * you want to keep the values that you made with {@link VelenCommand} but want to
     * update a specific part such as the description.
     * <br><br>
     * You can use {@link Velen#getAllSlashCommandIds(DiscordApi)} to retrieve the ID of the slash
     * command that you want to update.
     *
     * <br><br><b>We recommend using {@link pw.mihou.velen.internals.observer.VelenObserver} to
     * automate registration and updating of slash commands.</b>
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
    default CompletableFuture<Map<Long, String>> getAllSlashCommandIds(DiscordApi api) {
        return api.getGlobalSlashCommands().thenApply(slashCommands -> slashCommands.stream()
                .collect(Collectors.toMap(SlashCommand::getId, SlashCommand::getName)));
    }

    /**
     * This is used to check if the Velen instance currently
     * supports utilization of a blacklist (ignored user list).
     *
     * @return Does this Velen instance sport a blacklist?
     */
    default boolean supportsBlacklist() {
        return getBlacklist() != null;
    }

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
