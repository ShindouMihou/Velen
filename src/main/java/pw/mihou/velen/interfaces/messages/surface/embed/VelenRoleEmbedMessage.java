package pw.mihou.velen.interfaces.messages.surface.embed;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import pw.mihou.velen.interfaces.messages.VelenEmbedMessage;
import pw.mihou.velen.interfaces.messages.interfaces.VelenRoleOrdinaryMessageDelegate;

/**
 * This is an embed Velen Role Message that is sent
 * to the user whenever the user does not have the exact roles needed to
 * run a command. Embed means it will sent a message through an Embed.
 */
public interface VelenRoleEmbedMessage extends VelenEmbedMessage, VelenRoleOrdinaryMessageDelegate<EmbedBuilder> {
}
