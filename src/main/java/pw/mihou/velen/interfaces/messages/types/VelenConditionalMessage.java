package pw.mihou.velen.interfaces.messages.types;

import pw.mihou.velen.interfaces.messages.surface.embed.VelenConditionalEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenConditionalOrdinaryMessage;

/**
 * This message is intended to be used to
 * handle the "You do not meet the conditions to use this command" message that will
 * be sent to the user.
 */
public interface VelenConditionalMessage {

    /**
     * Creates a normal not-met conditions message that will be sent
     * to the user. Normal means a simple text message, if you want embed, please use
     * {@link VelenConditionalMessage#ofEmbed(VelenConditionalEmbedMessage)} )}.
     *
     * @param message The message to be sent to the user.
     * @return The message to be sent to the user (reflected).
     */
    static VelenConditionalMessage ofNormal(VelenConditionalOrdinaryMessage message) {
        return (VelenConditionalMessage) message;
    }

    /**
     * Creates a embed not-met conditions message that will be sent
     * to the user. Embed means the message will be sent as an embed,
     * if you want simple text, please use {@link VelenConditionalMessage#ofNormal(VelenConditionalOrdinaryMessage)} )}.
     *
     * @param message The message to be sent to the user.
     * @return The message to be sent to the user (reflected).
     */
    static VelenConditionalMessage ofEmbed(VelenConditionalEmbedMessage message) {
        return (VelenConditionalMessage) message;
    }

}
