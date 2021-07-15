package pw.mihou.velen.interfaces.messages.surface.embed;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import pw.mihou.velen.interfaces.messages.VelenEmbedMessage;
import pw.mihou.velen.interfaces.messages.interfaces.VelenRatelimitMessageDelegate;

/**
 * This is an ordinary Velen Ratelimit Message that is sent
 * to the user whenever the user is rate-limited. Embed means it will sent a message through an Embed.
 */
public interface VelenRatelimitEmbedMessage extends VelenEmbedMessage, VelenRatelimitMessageDelegate<EmbedBuilder> {
}
