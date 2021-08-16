package pw.mihou.velen.interfaces.hybrid.objects.subcommands;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import pw.mihou.velen.interfaces.hybrid.objects.VelenHybridArguments;
import pw.mihou.velen.interfaces.hybrid.objects.VelenOption;
import pw.mihou.velen.interfaces.hybrid.objects.interfaces.VelenCommonsArguments;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class VelenSubcommand implements VelenCommonsArguments {

    private final VelenOption[] options;
    private final VelenHybridArguments arguments;
    private final List<SlashCommandInteractionOption> provider;
    private final String[] args;

    public VelenSubcommand(int index, DiscordApi api, List<SlashCommandInteractionOption> provider,
                           String[] args, VelenHybridArguments arguments) {
        this.provider = provider;
        VelenOption[] t = null;
        AtomicInteger integer = new AtomicInteger(0);

        if(provider != null) {
            t = provider.stream()
                    .map(option -> new VelenOption(integer.getAndIncrement(), option, arguments))
                    .toArray(VelenOption[]::new);
        }

        if(args != null) {
            // We want to skip out anything that is beyond the subcommand.
            args = Arrays.copyOfRange(args, index, args.length);
            t = Arrays.stream(args)
                    .filter(s -> s.length() > 0)
                    .map(s -> new VelenOption(integer.getAndIncrement(), s, api, arguments))
                    .toArray(VelenOption[]::new);
        }


        // I don't know how this would be null but heck yeah.
        this.options = Objects.requireNonNull(t);
        this.arguments = arguments;
        this.args = args;
    }

    /**
     * Retrieves the parent arguments {@link VelenHybridArguments} of this subcommand.
     *
     * @return The parent arguments.
     */
    public VelenHybridArguments getParent() {
        return arguments;
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
