package pw.mihou.velen.interfaces.middleware.types;

import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.middleware.VelenGate;
import pw.mihou.velen.interfaces.middleware.VelenMiddleware;
import pw.mihou.velen.interfaces.routed.VelenRoutedOptions;
import pw.mihou.velen.utils.Pair;

public abstract class VelenMessageMiddleware implements VelenMiddleware {

    /**
     * This is ran before the command is executed, it is up to you here whether
     * to accept or reject the response. You can use the methods {@link VelenGate#deny()} or
     * {@link VelenGate#allow()} to accept or deny a request.
     *
     * @param event The event to hand over to the user.
     * @param command The command instance.
     * @param options The options that are incldued in this command.
     * @return The response whether to allow or reject the user.
     */
    public abstract Pair<Boolean, String> onEvent(MessageCreateEvent event, VelenCommand command, VelenRoutedOptions options, VelenGate gate);
    
}
