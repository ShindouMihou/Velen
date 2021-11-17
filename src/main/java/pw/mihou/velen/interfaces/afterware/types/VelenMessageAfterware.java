package pw.mihou.velen.interfaces.afterware.types;

import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.afterware.VelenAfterware;
import pw.mihou.velen.interfaces.routed.VelenRoutedOptions;

public interface VelenMessageAfterware extends VelenAfterware {

    /**
     * This is executed after the command is dispatched. You can run any sort of code here
     * as this is executed asynchronously.
     *
     * @param event The event that was received.
     * @param command The command that was triggered.
     * @param options The options included in this command.
     */
    void afterEvent(MessageCreateEvent event, VelenCommand command, VelenRoutedOptions options);

}
