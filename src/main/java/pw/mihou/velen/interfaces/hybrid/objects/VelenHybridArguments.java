package pw.mihou.velen.interfaces.hybrid.objects;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import pw.mihou.velen.interfaces.hybrid.objects.interfaces.VelenCommonsArguments;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class VelenHybridArguments implements VelenCommonsArguments {

    private final VelenOption[] options;
    private final List<SlashCommandInteractionOption> provider;
    private final String[] args;

    private VelenHybridArguments(DiscordApi api, List<SlashCommandInteractionOption> provider, String[] args) {
        this.provider = provider;
        this.args = args;

        AtomicInteger integer = new AtomicInteger(0);
        if(provider != null && args == null) {
            this.options = provider.stream()
                    .map(option -> new VelenOption(integer.getAndIncrement(), option, this))
                    .toArray(VelenOption[]::new);
        } else {
            this.options = Arrays.stream(args)
                    .filter(s -> s.length() > 0)
                    .map(s -> new VelenOption(integer.getAndIncrement(), s, api, this))
                    .toArray(VelenOption[]::new);
        }
    }

    /**
     * Creates a new Velen Hybrid Arguments that originated from a message command.
     *
     * @param args The arguments to use for the options.
     * @param api The API to use for the options (fetching, etc).
     */
    public VelenHybridArguments(String[] args, DiscordApi api) {
        // The length() filter is because of an issue where args has a length, somehow.
        this(api, null, args);
    }

    /**
     * Creates a new Velen Hybrid arguments that originated from a Slash Command.
     *
     * @param provider The provider to use for the options.
     */
    public VelenHybridArguments(List<SlashCommandInteractionOption> provider) {
        this(null, provider, null);
    }

    @Override
    public VelenOption[] getOptions() {
        return options;
    }

    @Override
    public Optional<List<SlashCommandInteractionOption>> asSlashCommandOptions() {
         return Optional.ofNullable(provider);
    }


    @Override
    public Optional<String[]> asMessageOptions() {
        return Optional.ofNullable(args);
    }

}
