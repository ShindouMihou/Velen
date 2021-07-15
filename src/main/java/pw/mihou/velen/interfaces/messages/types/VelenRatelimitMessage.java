package pw.mihou.velen.interfaces.messages.types;

import pw.mihou.velen.interfaces.messages.surface.embed.VelenRatelimitEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenRatelimitOrdinaryMessage;

/**
 * This message is intended to be used to
 * handle the "You are rate-limited" message that will
 * be sent to the user.
 */
public interface VelenRatelimitMessage {

    /**
     * Creates a normal rate-limit message that will be sent
     * to the user. Normal means a simple text message, if you want embed, please use
     * {@link VelenRatelimitMessage#ofEmbed(VelenRatelimitEmbedMessage)} )}.
     *
     * @param message The message to be sent to the user.
     * @return The message to be sent to the user (reflected).
     */
    static VelenRatelimitMessage ofNormal(VelenRatelimitOrdinaryMessage message) {
        return (VelenRatelimitMessage) message;
    }

    /**
     * Creates a embed rate-limit message that will be sent
     * to the user. Embed means the message will be sent as an embed, 
     * if you want simple text, please use {@link VelenRatelimitMessage#ofNormal(VelenRatelimitOrdinaryMessage)} )}.
     *
     * @param message The message to be sent to the user.
     * @return The message to be sent to the user (reflected).
     */
    static VelenRatelimitMessage ofEmbed(VelenRatelimitEmbedMessage message) {
        return (VelenRatelimitMessage) message;
    }

}
