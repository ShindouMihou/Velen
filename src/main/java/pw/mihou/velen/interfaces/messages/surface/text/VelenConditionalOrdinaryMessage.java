package pw.mihou.velen.interfaces.messages.surface.text;

import pw.mihou.velen.interfaces.messages.VelenOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.interfaces.VelenConditionalMessageDelegate;

/**
 * This is an ordinary Velen Conditional Message that is sent
 * to the user whenever the user does not have the exact conditions needed to
 * run a command. Ordinary means a simple text message.
 */
public interface VelenConditionalOrdinaryMessage extends VelenOrdinaryMessage, VelenConditionalMessageDelegate<String> {
}
