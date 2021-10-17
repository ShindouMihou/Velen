package pw.mihou.velen.interfaces.hybrid.responder.internal;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import pw.mihou.velen.interfaces.hybrid.responder.VelenGeneralResponder;
import pw.mihou.velen.interfaces.hybrid.responder.VelenResponderBase;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface VelenGeneralRespond<T> extends VelenResponderBase<T> {

    /**
     * Sends the response to the user or channel that triggered
     * this event, by default, if {@link VelenGeneralResponder#respondTo(TextChannel)} isn't set
     * then it will send to the channel or user that triggered the event.
     */
    CompletableFuture<Message> respond();

    /**
     * Gets the Interaction Original Response Updater that is being utilized by this
     * responder, if this event is triggered from a slash command.
     *
     * @return Gets the Interaction Original Response Updater that is being utilized by this
     * responder.
     */
    Optional<InteractionOriginalResponseUpdater> getInteractionOriginalResponseUpdater();

    /**
     * Retrieves the message builder that is being utilized by this
     * responder instance, you can use this to modify to your liking.
     *
     * @return The message builder being used.
     */
    Optional<MessageBuilder> getMessageBuilder();

}
