package pw.mihou.velen.interfaces.extensions;

import org.javacord.api.DiscordApi;
import pw.mihou.velen.impl.VelenImpl;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface VelenCompany {

    /**
     * This retrieves all the commands that matches.
     * @return The commands that matches.
     */
    List<VelenCommand> getCommands();

    /**
     * This finds a command that matches the name specified here.
     *
     * @param command The command to search for.
     * @return The first command that matches the name specified.
     */
    Optional<VelenCommand> getCommand(String command);

    /**
     * This finds a command that matches its index identification.
     *
     * @param id The command index to search for.
     * @return The first command that matches the index specified.
     */
    Optional<VelenCommand> getCommand(long id);

    /**
     * This finds any commands that matches the name and server specified.
     *
     * @param command The command's name to search for.
     * @param server The server that the command is attached on.
     * @return The first command that matches both tokens.
     */
    Optional<VelenCommand> getCommand(String command, long server);

    /**
     * Removes a command from Velen.
     *
     * @param command The command to remove.
     * @return The {@link Velen}  instance for chain-calling methods.
     */
    Velen removeCommand(VelenCommand command);

    /**
     * Adds a command to the registry, if {@link VelenImpl.Company#index(DiscordApi...)} has been
     * performed already then it will attach its own index.
     *
     * @param command The command to add to the registry.
     * @return The {@link Velen} instance for chain-calling methods.
     */
    Velen addCommand(VelenCommand command);

    /**
     * Performs a slash command indexing, this indexes any {@link VelenCommand} that
     * supports slash commands and isn't a server-specific command with their global id.
     *
     * @param shards The {@link DiscordApi} to use to collect the indexes.
     * @return A future that indicates completion or progress.
     */
    CompletableFuture<Void> index(DiscordApi... shards);

    /**
     * Performs a slash command indexing, this indexes any {@link VelenCommand} that
     * supports slash commands including server-specific commands if specified.
     *
     * @param shards The {@link DiscordApi} to use to collect the indexes.
     * @param allowServerIndexes Should server-commands be indexed as well?
     * @return A future that indicates completion or progress.
     */
    CompletableFuture<Void> index(boolean allowServerIndexes, DiscordApi... shards);

}
