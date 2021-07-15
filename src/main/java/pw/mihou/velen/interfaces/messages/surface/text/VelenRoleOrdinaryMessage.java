package pw.mihou.velen.interfaces.messages.surface.text;

import pw.mihou.velen.interfaces.messages.VelenOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.interfaces.VelenRoleOrdinaryMessageDelegate;

/**
 * This is an text Velen Role Message that is sent
 * to the user whenever the user does not have the exact roles needed to
 * run a command. Ordinary means a simple text message.
 */
public interface VelenRoleOrdinaryMessage extends VelenOrdinaryMessage, VelenRoleOrdinaryMessageDelegate<String> {
}
