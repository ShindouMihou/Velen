package pw.mihou.velen.interfaces.hybrid.objects.subcommands;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.hybrid.objects.VelenHybridArguments;
import pw.mihou.velen.interfaces.hybrid.objects.VelenOption;
import pw.mihou.velen.interfaces.hybrid.objects.interfaces.VelenCommonsArguments;
import pw.mihou.velen.internals.routing.VelenRoutedArgument;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class VelenSubcommand implements VelenCommonsArguments {

    private final VelenOption[] options;
    private final VelenHybridArguments arguments;
    private final List<SlashCommandInteractionOption> provider;
    private final VelenRoutedArgument[] args;
    private final String name;

    public VelenSubcommand(String name, int index, DiscordApi api, List<SlashCommandInteractionOption> provider,
                           VelenRoutedArgument[] args, VelenHybridArguments arguments, VelenCommand vl) {
        this.name = name;
        this.provider = provider;
        VelenOption[] t = null;
        AtomicInteger integer = new AtomicInteger(0);

        if(provider != null) {
            t = provider.stream()
                    .map(option -> new VelenOption(integer.getAndIncrement(), option, arguments, vl))
                    .toArray(VelenOption[]::new);
        }

        if(args != null) {
            // We want to skip out anything that is beyond the subcommand.
            args = Arrays.copyOfRange(args, index, args.length);
            t = Arrays.stream(args)
                    .filter(s -> s.getValue().length() > 0)
                    .map(s -> new VelenOption(integer.getAndIncrement(), s, api, arguments, vl))
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

    /**
     * Gets the name of the sub command.
     *
     * @return The name value of the subcommand.
     */
    public String getName() {
        return name;
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
        return Optional.ofNullable(args)
                .map(o -> Arrays.stream(o).map(VelenRoutedArgument::getValue).toArray(String[]::new));
    }

    @Override
    public Optional<VelenRoutedArgument[]> asRoutedArguments() {
        return Optional.ofNullable(args);
    }

}
