package pw.mihou.velen.interfaces;

import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.util.List;

public interface VelenSlashEvent {

    /**
     * This is called when the exact command is executed.
     *
     * @param originalEvent  The base event received from Javacord.
     * @param event          The event received from Javacord.
     * @param args           The arguments received from the event's message.
     * @param options        The raw list of arguments received from the event.
     * @param user           The user who executed the command.
     * @param firstResponder The initial response sent to the user, this is required
     *                       to answer, you can choose to ignore this and instead use
     *                       {@link SlashCommandInteraction#respondLater()} to tell
     *                       Discord that you will respond later.
     */
    void onEvent(SlashCommandCreateEvent originalEvent, SlashCommandInteraction event, User user, VelenArguments args, List<SlashCommandInteractionOption> options,
                 InteractionImmediateResponseBuilder firstResponder);

}
