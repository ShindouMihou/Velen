package pw.mihou.velen.interfaces.hybrid.objects.interfaces;

import org.javacord.api.interaction.SlashCommandInteractionOption;
import pw.mihou.velen.interfaces.hybrid.objects.VelenOption;
import pw.mihou.velen.internals.routing.VelenRoutedArgument;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface VelenCommonsArguments {

    /**
     * Retrieves all the options available.
     *
     * @return All the options available.
     */
    VelenOption[] getOptions();

    /**
     * Gets the amount of options that this event contains.
     *
     * @return The amount of options that this event contains.
     */
    default int length() {
        return getOptions().length;
    }

    /**
     * Gets the option at the specific index.
     *
     * @param index The index to fetch.
     * @return The Velen Option at the index specified.
     */
    default VelenOption get(int index) {
        return getOptions()[index];
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command.
     *
     * @param name The name of the argument to find.
     * @return The Velen Option bearing the specific name.
     */
    default Optional<VelenOption> withName(String name) {
        return Arrays.stream(getOptions())
                .filter(velenOption -> velenOption.getName() != null && velenOption.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Combines all the values of the same name (usually this is the same option) and
     * joins them with spaces to make it seem like a single option. For instance:
     * <br>
     * We have the text: Hello World with the format `say :[text]`, it would work but it would only
     * return `Hello` which is not the desired. Turning `:[text:hasMany()]` would then allow this to work and
     * the option `text` would be able to grab all the text which in this case would be "Hello World"
     *
     * @param name The name of the option.
     * @return The string value of the options.
     */
    default Optional<String> getManyWithName(String name) {
        long duplicates = Arrays.stream(getOptions())
                .filter(arg -> arg.getName() != null && arg.getName().equalsIgnoreCase(name))
                .filter(arg -> arg.asString().isPresent())
                .count();

        if (duplicates < 2) {
            return Arrays.stream(getOptions())
                    .filter(arg -> arg.getName() != null && arg.getName().equalsIgnoreCase(name))
                    .map(VelenOption::asString)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        } else {
            // Allow us to return all the values.
            return Optional.of(Arrays.stream(getOptions())
                    .filter(arg -> arg.getName() != null && arg.getName().equalsIgnoreCase(name))
                    .map(VelenOption::asString)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.joining(" ")));
        }
    }

    /**
     * Retrieves the slash command options from this event.
     *
     * @return The slash command options from this event.
     */
    Optional<List<SlashCommandInteractionOption>> asSlashCommandOptions();

    /**
     * Retrieves the message command options from this event.
     *
     * @return The message options from this event.
     */
    Optional<String[]> asMessageOptions();

    /**
     * Retrieves the message command options from this event as routed
     * arguments.
     *
     * @return The routed mesasge arguments from this event.
     */
    Optional<VelenRoutedArgument[]> asRoutedArguments();

}
