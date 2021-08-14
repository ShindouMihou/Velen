package pw.mihou.velen.interfaces;

import org.javacord.api.entity.user.User;
import pw.mihou.velen.interfaces.hybrid.event.VelenGeneralEvent;
import pw.mihou.velen.interfaces.hybrid.objects.VelenHybridArguments;
import pw.mihou.velen.interfaces.hybrid.responder.VelenGeneralResponder;

public interface VelenHybridHandler {

    /**
     * This is called when the exact command is executed.
     *
     * @param event The general event (this allows you to manage both Message and Slash events).
     * @param responder The general responder (if this is a slash command event, please note that by the time the event is received,
     *                  the timer for the 15 minutes respond limit from Discord is already ticking).
     * @param user The user who triggered the event.
     * @param args The arguments that were included with this event.
     */
    void onEvent(VelenGeneralEvent event, VelenGeneralResponder responder, User user, VelenHybridArguments args);

}
