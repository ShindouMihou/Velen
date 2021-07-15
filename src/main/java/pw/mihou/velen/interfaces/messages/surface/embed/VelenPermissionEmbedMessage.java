package pw.mihou.velen.interfaces.messages.surface.embed;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import pw.mihou.velen.interfaces.messages.VelenEmbedMessage;
import pw.mihou.velen.interfaces.messages.VelenOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.interfaces.VelenPermissionMessageDelegate;

/**
 * This is an embed Velen Permission Message that is sent
 * to the user whenever the user does not have the exact permissions needed to
 * run a command. Embed means it will sent a message through an Embed.
 */
public interface VelenPermissionEmbedMessage extends VelenEmbedMessage, VelenPermissionMessageDelegate<EmbedBuilder> {
}
