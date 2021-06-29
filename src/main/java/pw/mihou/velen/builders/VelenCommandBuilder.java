package pw.mihou.velen.builders;

import org.javacord.api.entity.permission.PermissionType;
import pw.mihou.velen.impl.VelenCommandImpl;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.VelenEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VelenCommandBuilder {

    private final List<Long> requiredRoles = new ArrayList<>();
    private final List<Long> requiredUsers = new ArrayList<>();
    private final List<PermissionType> permissions = new ArrayList<>();
    private final List<String> shortcuts = new ArrayList<>();
    private String category;
    private String name;
    private String usage;
    private String description;
    private Duration cooldown;
    private boolean serverOnly = false;
    private VelenEvent velenEvent;
    private Velen velen;

    /**
     * Sets the name of the command.
     *
     * @param commandName The name of the command.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder setName(String commandName) {
        this.name = commandName;
        return this;
    }

    /**
     * Sets the usage of the command.
     *
     * @param usage The usage of the command.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    /**
     * Sets the description of the command.
     *
     * @param description The description of the command.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the category of the command.
     *
     * @param category The category of the command.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder setCategory(String category) {
        this.category = category;
        return this;
    }

    /**
     * Sets the cooldown of the command, to disable, use <b>Duration.of(0, TemporalUnit.SECONDS)</b>
     * otherwise do not set if you want it to use the default cooldown that is set with Velen.
     *
     * @param duration The cooldown of the command.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder setCooldown(Duration duration) {
        this.cooldown = duration;
        return this;
    }

    /**
     * Make this command only useable to a certain role.
     *
     * @param roleID The ID of the role that users must have to use the command.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder requireRole(long roleID) {
        requiredRoles.add(roleID);
        return this;
    }

    /**
     * Make this command only useable to certain roles.
     *
     * @param roles The roles that the users must have to use the command.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder requireRoles(long... roles) {
        Arrays.stream(roles).forEach(requiredRoles::add);
        return this;
    }

    /**
     * Make this command only useable to a user.
     *
     * @param user The user who can use this command.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder requireUser(long user) {
        requiredUsers.add(user);
        return this;
    }

    /**
     * Makes this command only useable to certain users.
     *
     * @param users The users who can use this command.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder requireUsers(long... users) {
        Arrays.stream(users).forEach(requiredUsers::add);
        return this;
    }

    /**
     * Makes this command require a certain permission.
     *
     * @param permissionType The permission to require.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder requirePermission(PermissionType permissionType) {
        permissions.add(permissionType);
        return this;
    }

    /**
     * Makes this command require the following permissions.
     *
     * @param perms The permissions to require.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder requirePermissions(PermissionType... perms) {
        permissions.addAll(Arrays.asList(perms));
        return this;
    }

    /**
     * Adds a shortcut that will be used, for example, if you have
     * a command name of "help" and you want to use the shortcut "hel"
     * then you can add it here.
     *
     * @param shortcut The shortcut to add.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder addShortcut(String shortcut) {
        shortcuts.add(shortcut);
        return this;
    }

    /**
     * Adds multiple shortcuts that will be used, for example, if you have
     * a command name of "help" and you want to use the shortcuts "hel", "he", and "h"
     * then you can add them here.
     *
     * @param shortcuts The shortcuts to add.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder addShortcuts(String... shortcuts) {
        this.shortcuts.addAll(Arrays.asList(shortcuts));
        return this;
    }

    /**
     * Sets the handler for when the command is invoked, <b>THIS IS REQUIRED TO SET</b>.
     * You must set this one as this one will be executed when the command is invoked by a user.
     *
     * @param event The Velen Event to use when the command is invoked.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder doEventOnInvocation(VelenEvent event) {
        this.velenEvent = event;
        return this;
    }

    /**
     * Should this command be server only?
     *
     * @param serverOnly Is this command a server only command?
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder setServerOnly(boolean serverOnly) {
        this.serverOnly = serverOnly;
        return this;
    }

    /**
     * Sets the Velen to use (this will make the command use the Velen's ratelimiter, etc).
     *
     * @param velen The velen to use.
     * @return VelenCommandBuilder for chain calling methods.
     */
    public VelenCommandBuilder setVelen(Velen velen) {
        this.velen = velen;
        return this;
    }

    /**
     * Attaches the command directly onto Velen.
     */
    public void attach() {
        if (velen == null)
            throw new IllegalArgumentException("You cannot attach a VelenCommand with a null Velen!");

        velen.addCommand(build());
    }

    /**
     * Creates a VelenCommand which you can then attach to Velen
     * or retrieve the values from.
     *
     * @return A velen command.
     */
    public VelenCommand build() {

        if (velen == null)
            throw new IllegalArgumentException("Velen cannot be null when creating a command!");

        if (velenEvent == null)
            throw new IllegalArgumentException("Velen Event cannot be null when creating a command since it will be executed" +
                    " when the command is triggered!");

        if (cooldown == null)
            cooldown = velen.getRatelimiter().getDefaultCooldown();

        if (description == null)
            description = "No description";

        if (usage == null)
            usage = "";

        if(category == null)
            category = "";

        return new VelenCommandImpl(name, usage, description, category, cooldown, requiredRoles, requiredUsers,
                permissions, serverOnly, shortcuts, velenEvent, velen);

    }

}
