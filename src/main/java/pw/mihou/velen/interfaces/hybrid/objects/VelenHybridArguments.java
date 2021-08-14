package pw.mihou.velen.interfaces.hybrid.objects;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class VelenHybridArguments {

    private final VelenOption[] options;
    private final List<SlashCommandInteractionOption> provider;
    private final String[] args;

    private VelenHybridArguments(VelenOption[] options, List<SlashCommandInteractionOption> provider, String[] args) {
        this.options = options;
        this.provider = provider;
        this.args = args;
    }

    /**
     * Creates a new Velen Hybrid Arguments that originated from a message command.
     *
     * @param args The arguments to use for the options.
     * @param api The API to use for the options (fetching, etc).
     */
    public VelenHybridArguments(String[] args, DiscordApi api) {
        // The length() filter is because of an issue where args has a length, somehow.
        this(Arrays.stream(args)
                .filter(s -> s.length() > 0)
                .map(s -> new VelenOption(s, api)).toArray(VelenOption[]::new), null, args);
    }

    /**
     * Creates a new Velen Hybrid arguments that originated from a Slash Command.
     *
     * @param provider The provider to use for the options.
     */
    public VelenHybridArguments(List<SlashCommandInteractionOption> provider) {
        this(provider.stream().map(VelenOption::new).toArray(VelenOption[]::new), provider, null);
    }

    /**
     * Retrieves all the options available.
     *
     * @return All the options available.
     */
    public VelenOption[] getOptions() {
        return options;
    }

    /**
     * Gets the amount of options that this event contains.
     *
     * @return The amount of options that this event contains.
     */
    public int length() {
        return getOptions().length;
    }

    /**
     * Gets the option at the specific index.
     *
     * @param index The index to fetch.
     * @return The Velen Option at the index specified.
     */
    public VelenOption get(int index) {
        return options[index];
    }

    /**
     * Retrieves the slash command options from this event.
     *
     * @return The slash command options from this event.
     */
    public Optional<List<SlashCommandInteractionOption>> asSlashCommandOptions() {
         return Optional.ofNullable(provider);
    }

    /**
     * Retrieves the message command options from this event.
     *
     * @return The message options from this event.
     */
    public Optional<String[]> asMessageOptions() {
        return Optional.ofNullable(args);
    }

}
