package pw.mihou.velen.interfaces.afterware.types;

import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.afterware.VelenAfterware;
import pw.mihou.velen.interfaces.hybrid.event.VelenGeneralEvent;
import pw.mihou.velen.interfaces.hybrid.objects.VelenHybridArguments;

public interface VelenHybridAfterware extends VelenAfterware {

    /**
     * This is executed after the command is dispatched. You can run any sort of code here
     * as this is executed asynchronously. Do note that responding to the events here will
     * cause errors if the command has thrown a response ahead of time.
     *
     * @param event The event that was received.
     * @param arguments The arguments received in the command.
     * @param command The command that was triggered.
     */
    void afterEvent(VelenGeneralEvent event, VelenHybridArguments arguments, VelenCommand command);

}
