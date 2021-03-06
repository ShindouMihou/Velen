package pw.mihou.velen.interfaces.middleware.types;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.middleware.VelenGate;
import pw.mihou.velen.interfaces.middleware.VelenMiddleware;
import pw.mihou.velen.utils.Pair;

public interface VelenSlashMiddleware extends VelenMiddleware {

    /**
     * This is ran before the command is executed, it is up to you here whether
     * to accept or reject the response. You can use the methods {@link VelenGate#deny()} or
     * {@link VelenGate#allow()} to accept or deny a request.
     *
     * @param event The event to hand over to the user.
     * @param command The command instance.
     * @param gate The gate that is responsible for creating whether the command can execute further or not.
     * @return The response whether to allow or reject the user.
     */
    Pair<Boolean, String> onEvent(SlashCommandCreateEvent event, VelenCommand command, VelenGate gate);
    
}
