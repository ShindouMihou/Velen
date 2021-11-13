package pw.mihou.velen.interfaces.middleware.types;

import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.hybrid.event.VelenGeneralEvent;
import pw.mihou.velen.interfaces.hybrid.objects.VelenHybridArguments;
import pw.mihou.velen.interfaces.middleware.VelenGate;
import pw.mihou.velen.interfaces.middleware.VelenMiddleware;
import pw.mihou.velen.utils.Pair;

@FunctionalInterface
public interface VelenHybridMiddleware extends VelenMiddleware {

    /**
     * This is ran before the command is executed, it is up to you here whether
     * to accept or reject the response. You can use the methods {@link VelenGate#deny()} or
     * {@link VelenGate#allow()} to accept or deny a request.
     *
     * @param event The event to hand over to the user.
     * @param arguments The arguments involved in this command.
     * @param command The command instance.
     * @return The response whether to allow or reject the user.
     */
    Pair<Boolean, String> onEvent(VelenGeneralEvent event, VelenHybridArguments arguments, VelenCommand command, VelenGate gate);
    
}
