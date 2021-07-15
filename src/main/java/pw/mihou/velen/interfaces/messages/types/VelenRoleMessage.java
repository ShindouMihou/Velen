package pw.mihou.velen.interfaces.messages.types;

import pw.mihou.velen.interfaces.messages.surface.embed.VelenRoleEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenRoleOrdinaryMessage;

/**
 * This message is intended to be used to
 * handle the "You do not have the role(s) to use this command" message that will
 * be sent to the user.
 */
public interface VelenRoleMessage {

    /**
     * Creates a normal no-role message that will be sent
     * to the user. Normal means a simple text message, if you want embed, please use
     * {@link VelenRoleMessage#ofEmbed(VelenRoleEmbedMessage)} )}.
     *
     * @param message The message to be sent to the user.
     * @return The message to be sent to the user (reflected).
     */
    static VelenRoleMessage ofNormal(VelenRoleOrdinaryMessage message) {
        return (VelenRoleMessage) message;
    }

    /**
     * Creates a embed no-role message that will be sent
     * to the user. Embed means the message will be sent as an embed,
     * if you want simple text, please use {@link VelenRoleMessage#ofNormal(VelenRoleOrdinaryMessage)} )}.
     *
     * @param message The message to be sent to the user.
     * @return The message to be sent to the user (reflected).
     */
    static VelenRoleMessage ofEmbed(VelenRoleEmbedMessage message) {
        return (VelenRoleMessage) message;
    }

}
