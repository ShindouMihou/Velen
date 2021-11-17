package pw.mihou.velen.interfaces.afterware.types;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.afterware.VelenAfterware;

public interface VelenSlashAfterware extends VelenAfterware {

    /**
     * This is executed after the command is dispatched. You can run any sort of code here
     * as this is executed asynchronously. Do note that responding to the events here will
     * cause errors if the command has thrown a response ahead of time.
     *
     * @param event The event that was received.
     * @param command The command that was triggered.
     */
    void afterEvent(SlashCommandCreateEvent event, VelenCommand command);

}
