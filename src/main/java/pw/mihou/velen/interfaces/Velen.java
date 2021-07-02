package pw.mihou.velen.interfaces;

import com.sun.istack.internal.Nullable;
import org.javacord.api.DiscordApi;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;
import pw.mihou.velen.VelenBuilder;
import pw.mihou.velen.builders.VelenMessage;
import pw.mihou.velen.builders.VelenPermissionMessage;
import pw.mihou.velen.builders.VelenRoleMessage;
import pw.mihou.velen.internals.VelenBlacklist;
import pw.mihou.velen.prefix.VelenPrefixManager;
import pw.mihou.velen.ratelimiter.VelenRatelimiter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
    VelenMessage getRatelimitedMessage();

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
     * Gets a certain command through its name (ignoring casing).
     *
     * @param command The command to find.
     * @return An optional that possibly contains the command.
     */
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
    @Nullable
     VelenBlacklist getBlacklist();

    /**
     * Gets the Prefix Manager used by the Velen.
     *
     * @return The prefix manager used.
     */
    VelenPrefixManager getPrefixManager();

}
