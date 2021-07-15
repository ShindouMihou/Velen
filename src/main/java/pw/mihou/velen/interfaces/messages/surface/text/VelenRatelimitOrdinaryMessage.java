package pw.mihou.velen.interfaces.messages.surface.text;

import pw.mihou.velen.interfaces.messages.VelenOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.interfaces.VelenRatelimitMessageDelegate;

/**
 * This is an ordinary Velen Ratelimit Message that is sent
 * to the user whenever the user is rate-limited. Ordinary means a simple text message.
 */
public interface VelenRatelimitOrdinaryMessage extends VelenOrdinaryMessage, VelenRatelimitMessageDelegate<String> {
}
