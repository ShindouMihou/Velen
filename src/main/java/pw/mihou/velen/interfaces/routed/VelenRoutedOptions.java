package pw.mihou.velen.interfaces.routed;

import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.impl.VelenCommandImpl;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.internals.routing.VelenRoutedArgument;
import pw.mihou.velen.internals.routing.VelenUnderscoreParser;

import java.util.Arrays;
import java.util.Optional;

public class VelenRoutedOptions {

    private final VelenRoutedArgument[] arguments;
    private final VelenCommand command;

    public VelenRoutedOptions(VelenCommand command, MessageCreateEvent event) {
        this.command = command;

        String[] commandIndexes = event.getMessageContent().split("\\s+");
        VelenRoutedArgument[] vArgs = VelenUnderscoreParser.route(event.getMessageContent(), ((VelenCommandImpl)command).getFormats())
                .entrySet()
                .stream()
                .map(entry -> new VelenRoutedArgument(entry.getKey(), entry.getValue(), commandIndexes[entry.getKey()]))
                .toArray(VelenRoutedArgument[]::new);

        System.out.println("Parsed the command; " + event.getMessageContent() + " with " + vArgs.length);
        arguments = vArgs;
    }

    /**
     * Retrieves the option with the specific name, this can return empty especially
     * if there isn't any format specified for the command.
     *
     * @param name The name of the argument to find.
     * @return The value of the option in the specific position.
     */
    public Optional<String> withName(String name) {
        return Arrays.stream(arguments)
                .filter(arg -> arg.getName() != null && arg.getName().equalsIgnoreCase(name))
                .map(VelenRoutedArgument::getValue)
                .findFirst();
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
