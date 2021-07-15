package pw.mihou.velen.interfaces.messages.types;

import pw.mihou.velen.interfaces.messages.surface.embed.VelenPermissionEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.embed.VelenRatelimitEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenPermissionOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenRatelimitOrdinaryMessage;

/**
 * This message is intended to be used to
 * handle the "You do not have the permission to use this command" message that will
 * be sent to the user.
 */
public interface VelenPermissionMessage {

    /**
     * Creates a normal no-permission message that will be sent
     * to the user. Normal means a simple text message, if you want embed, please use
     * {@link VelenPermissionMessage#ofEmbed(VelenPermissionEmbedMessage)} )}.
     *
     * @param message The message to be sent to the user.
     * @return The message to be sent to the user (reflected).
     */
    static VelenPermissionMessage ofNormal(VelenPermissionOrdinaryMessage message) {
        return (VelenPermissionMessage) message;
    }

    /**
     * Creates a embed no-permission message that will be sent
     * to the user. Embed means the message will be sent as an embed,
     * if you want simple text, please use {@link VelenPermissionMessage#ofNormal(VelenPermissionOrdinaryMessage)} )}.
     *
     * @param message The message to be sent to the user.
     * @return The message to be sent to the user (reflected).
     */
    static VelenPermissionMessage ofEmbed(VelenPermissionEmbedMessage message) {
        return (VelenPermissionMessage) message;
    }

}
