package pw.mihou.velen;

import pw.mihou.velen.builders.VelenMessage;
import pw.mihou.velen.builders.VelenPermissionMessage;
import pw.mihou.velen.builders.VelenRoleMessage;
import pw.mihou.velen.impl.VelenImpl;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.internals.VelenBlacklist;
import pw.mihou.velen.prefix.VelenPrefixManager;
import pw.mihou.velen.ratelimiter.VelenRatelimiter;

import java.time.Duration;
import java.util.stream.Collectors;

public class VelenBuilder {

    private VelenMessage ratelimitMessage = (remainingSeconds, user, channel, command) -> "You can use this command in **" + remainingSeconds + " seconds**, " +
            "during this period, the bot will not respond to to any invocation of the command: **" + command + "** for the user. " +
            "This message will delete itself when cooldown is over.";
    private VelenPermissionMessage noPermissionMessage = (permission, user, channel, command) -> "You need these permission(s): " + permission
            .stream().map(Enum::name).collect(Collectors.joining(", ")) + " to run this command!";
    private VelenRoleMessage noRoleMessage = (role, user, channel, command) -> "You need to have any of the role(s): " + role + " to run this command!";
    private VelenRatelimiter ratelimiter = new VelenRatelimiter();
    private VelenPrefixManager prefixManager = new VelenPrefixManager("v.");
    private VelenBlacklist blacklist;
    private boolean allowMentionPrefix = true;

    /**
     * Sets the default prefix to use, this is by default, <b>v.</b>
     * Please note that this will be overriden if you use {@link VelenBuilder#setPrefixManager(VelenPrefixManager)}
     * since we will be using the prefix manager you set.
     *
     * @param defaultPrefix The default prefix to use.
     * @return VelenBuilder for chain calling methods.
     */
    public VelenBuilder setDefaultPrefix(String defaultPrefix) {
        this.prefixManager = new VelenPrefixManager(defaultPrefix);
        return this;
    }

    /**
     * Sets the default cooldown time of the rate-limiter, this will
     * be used if a command has no default cooldown.
     *
     * @param duration The default amount of time for a command's cooldown.
     * @return VelenBuilder for chain calling methods.
     */
    public VelenBuilder setDefaultCooldownTime(Duration duration) {
        this.ratelimiter = new VelenRatelimiter(duration);
        return this;
    }

    /**
     * Sets the message to be sent when a user attempts to execute a command while
     * rate-limited, it will only be sent once for every rate-limit cycle.
     *
     * @param ratelimitMessage The message to send.
     * @return VelenBuilder for chain calling methods.
     */
    public VelenBuilder setRatelimitedMessage(VelenMessage ratelimitMessage) {
        this.ratelimitMessage = ratelimitMessage;
        return this;
    }

    /**
     * Sets the message to be sent when a user attempts to execute a command while
     * not having the required role to use the command.
     *
     * @param noRoleMessage The message to send.
     * @return VelenBuilder for chain calling methods.
     */
    public VelenBuilder setNoRoleMessage(VelenRoleMessage noRoleMessage) {
        this.noRoleMessage = noRoleMessage;
        return this;
    }

    /**
     * Sets the blacklist to use by {@link Velen}, this will be used by {@link Velen} to
     * check if the user is ignored or not.<br>
     * <p>
     * We highly recommend that if you are not using the blacklist, set this to
     * null since that will tell {@link Velen} that you are not using one and so the application
     * does not have to check.
     *
     * @param blacklist The {@link VelenBlacklist} to use.
     * @return VelenBuilder for chain calling methods.
     */
    public VelenBuilder setBlacklist(VelenBlacklist blacklist) {
        this.blacklist = blacklist;
        return this;
    }

    /**
     * Sets the message to be sent when a user attempts to execute a command while
     * not having the required permissions to use the command.
     *
     * @param noPermissionMessage The message to send.
     * @return VelenBuilder for chain calling methods.
     */
    public VelenBuilder setNoPermissionMessage(VelenPermissionMessage noPermissionMessage) {
        this.noPermissionMessage = noPermissionMessage;
        return this;
    }

    /**
     * Sets the {@link VelenPrefixManager} to use for providing prefixes, this method is usually
     * used if you want to use a per-server prefix manager, in which you would then
     * provide a {@link pw.mihou.velen.prefix.loaders.PrefixLoader}
     *
     * @param prefixManager The Prefix Manager that will be used to provide prefixes.
     * @return VelenBuilder for chain calling methods.
     */
    public VelenBuilder setPrefixManager(VelenPrefixManager prefixManager) {
        this.prefixManager = prefixManager;
        return this;
    }

    /**
     * Sets whether to allow Velen to respond to prefixes that only mentions the bot, this is
     * usually enabled by default in compliance of the best practices that has circulated online.
     * <p>
     * An example of this would be: <code>@Velen help</code>
     * or <code>@Velen help someCommand</code>
     *
     * @param allow Whether to allow the bot to respond to mention as prefixes.
     * @return VelenBuilder for chain calling methods.
     */
    public VelenBuilder allowMentionAsPrefix(boolean allow) {
        this.allowMentionPrefix = allow;
        return this;
    }

    /**
     * Builds the Velen component which you can then use
     * to add commands, etc.
     *
     * @return the Velen component.
     */
    public Velen build() {
        return new VelenImpl(ratelimiter, prefixManager, ratelimitMessage, noPermissionMessage, noRoleMessage, blacklist, allowMentionPrefix);
    }

}
