package pw.mihou.velen.interfaces.hybrid.event;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.CertainMessageEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageEvent;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.hybrid.objects.VelenHybridArguments;
import pw.mihou.velen.interfaces.hybrid.responder.VelenGeneralResponder;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public interface VelenGeneralEvent {

    /**
     * Retrieves the Velen command instance that is in charge
     * of this event.
     *
     * @return The Velen Command instance.
     */
    VelenCommand getCommand();

    /**
     * Retrieves the name of the command that was triggered.
     *
     * @return The name of the command.
     */
    String getCommandName();

    /**
     * Retrieves the original slash command event of this general event.
     *
     * @return The original slash command event.
     */
    Optional<SlashCommandCreateEvent> asSlashEvent();

    /**
     * Retrieves the original response updater that is being utilized by this event, please note that by
     * the time that the event is received, this updater is already counting down.
     *
     * @return The interaction original response updater that is being utilized.
     */
    Optional<InteractionOriginalResponseUpdater> getUpdater();

    /**
     * Creates a Velen General Responder which you should use to respond to the
     * user.
     *
     * @return The {@link VelenGeneralResponder} for this event.
     */
    default VelenGeneralResponder createResponder() {
        return new VelenGeneralResponder(asMessageEvent().orElse(null), getUpdater().orElse(null));
    }

    /**
     * Retrieves the original message event of this general event.
     *
     * @return The original message event.
     */
    Optional<MessageCreateEvent> asMessageEvent();

    /**
     * Was this event triggered by a slash command?
     *
     * @return Was this command triggered by a slash command?
     */
    default boolean isSlashEvent() {
        return asSlashEvent().isPresent();
    }

    /**
     * Was this event triggered by a message command?
     *
     * @return Was this event triggered by a message command?
     */
    default boolean isMessageEvent() {
        return asMessageEvent().isPresent();
    }

    /**
     * Gets the arguments that were received by this event.
     *
     * @return The arguments that were received.
     */
    VelenHybridArguments getArguments();

    /**
     * Gets the message that triggered this command, this only has a value
     * if the command was a message command.
     *
     * @return The message that triggered this event.
     */
    default Optional<Message> getMessage() {
        return asMessageEvent().map(CertainMessageEvent::getMessage);
    }

    /**
     * Gets the message content of this event, this only has a value
     * if the command was a message command.
     *
     * @return The message content of this event.
     */
    default Optional<String> getMessageContent() {
        return asMessageEvent().map(CertainMessageEvent::getMessageContent);
    }

    /**
     * Gets the readable message content of this event, this only has a value
     * if the command was a message command.
     *
     * @return The readable message content of this event.
     */
    default Optional<String> getReadableMessageContent() {
        return asMessageEvent().map(CertainMessageEvent::getReadableMessageContent);
    }

    /**
     * Gets the message link of this event, this only has a value
     * if the command was a message command.
     *
     * @return The message link of this event.
     */
    default Optional<URL> getMessageLink() {
        return asMessageEvent().map(CertainMessageEvent::getMessageLink);
    }

    /**
     * Gets the message attachments of this event, this only has a value
     * if the command was a message command.
     *
     * @return The message attachments of this event.
     */
    default Optional<List<MessageAttachment>> getMessageAttachments() {
        return asMessageEvent().map(CertainMessageEvent::getMessageAttachments);
    }

    /**
     * Gets the message id of this event, this only has a value
     * if the command was a message command.
     *
     * @return The message id of this event.
     */
    default Optional<Long> getMessageId() {
        return asMessageEvent().map(MessageEvent::getMessageId);
    }

    /**
     * Gets the message author of this event, this only has a value
     * if the command was a message command.
     *
     * @return The message author of this event.
     */
    default Optional<MessageAuthor> getMessageAuthor() {
        return getMessage().map(Message::getAuthor);
    }

    /**
     * Gets the user who triggered the command.
     *
     * @return The user who triggered the command.
     */
    // I don't know what is the warning value for this one
    // but Velen ensures that the user should be available always, so yeah.
    @SuppressWarnings("all")
    default User getUser() {
        return isSlashEvent() ? asSlashEvent().get().getInteraction().getUser() : getMessageAuthor().get().asUser().get();
    }


    /**
     * Gets the server where this command was triggered.
     *
     * @return The server where the command was triggered.
     */
    default Optional<Server> getServer() {
        if(isSlashEvent())
            return asSlashEvent().flatMap(e -> e.getInteraction().getServer());

        return asMessageEvent().flatMap(MessageEvent::getServer);
    }

    /**
     * Gets the text channel where the command was triggered.
     *
     * @return The text channel where this command was triggered.
     */
    @SuppressWarnings("all")
    default TextChannel getChannel() {
        // Velen validates that all events should have an event, so yeah.
        if(isSlashEvent())
            return asSlashEvent().flatMap(e -> e.getSlashCommandInteraction().getChannel()).get();

        return asMessageEvent().get().getChannel();
    }


}
