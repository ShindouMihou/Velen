package pw.mihou.velen.interfaces.messages.surface.text;

import pw.mihou.velen.interfaces.messages.VelenOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.interfaces.VelenPermissionMessageDelegate;

/**
 * This is an ordinary Velen Permission Message that is sent
 * to the user whenever the user does not have the exact permissions needed to
 * run a command. Ordinary means a simple text message.
 */
public interface VelenPermissionOrdinaryMessage extends VelenOrdinaryMessage, VelenPermissionMessageDelegate<String> {
}
