package pw.mihou.velen.interfaces.messages.surface.embed;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import pw.mihou.velen.interfaces.messages.VelenEmbedMessage;
import pw.mihou.velen.interfaces.messages.interfaces.VelenConditionalMessageDelegate;

/**
 * This is an embed Velen Conditioanl Message that is sent
 * to the user whenever the user does not meet the conditions needed to
 * run a command. Embed means it will sent a message through an Embed.
 */
public interface VelenConditionalEmbedMessage extends VelenEmbedMessage, VelenConditionalMessageDelegate<EmbedBuilder> {
}
