package pw.mihou.velen.interfaces;

import org.javacord.api.entity.permission.PermissionType;
import pw.mihou.velen.builders.VelenCommandBuilder;

import java.time.Duration;
import java.util.List;

public interface VelenCommand {

    /**
     * Creates a new Velen command builder with all
     * the required parameters filled.
     *
     * @param name  The name of the command.
     * @param event The event to execute when the command is invoked.
     * @param velen The velen instance (which will be used to grab default rate-limiter, etc).
     * @return A VelenCommandBuilder in case you want to add more options.
     */
    static VelenCommandBuilder of(String name, Velen velen, VelenEvent event) {
        return new VelenCommandBuilder().setName(name)
                .doEventOnInvocation(event).setVelen(velen);
    }

    /**
     * Creates a new Velen command builder with all
     * the required parameters filled with an extra description.
     *
     * @param name        The name of the command.
     * @param description The description of the command.
     * @param event       The event to execute when the command is invoked.
     * @param velen       The velen instance (which will be used to grab default rate-limiter, etc).
     * @return A VelenCommandBuilder in case you want to add more options.
     */
    static VelenCommandBuilder of(String name, String description, Velen velen, VelenEvent event) {
        return new VelenCommandBuilder().setName(name).setDescription(description)
                .doEventOnInvocation(event).setVelen(velen);
    }

    /**
     * Gets the name of the command.
     *
     * @return The name of the command.
     */
    String getName();

    /**
     * Gets the usage of the command.
     *
     * @return The usage of the command.
     */
    String getUsage();

    /**
     * Gets the command description.
     *
     * @return The command description.
     */
    String getDescription();

    /**
     * Gets the cooldown duration of the command.
     *
     * @return The cooldown duration of the command.
     */
    Duration getCooldown();

    /**
     * Gets the roles required to users to use this command, by default, is empty
     * which means everyone can use it.
     *
     * @return The roles required to users to use
     */
    List<Long> getRequiredRoles();

    /**
     * Gets the users who can use this command, by default, is empty
     * which means everyone can use it.
     *
     * @return The users required to run the command.
     */
    List<Long> getRequiredUsers();

    /**
     * Gets all the permissions required to run the command, by default, is empty
     * which means everyone can use it.
     *
     * @return The permissions required to run the command.
     */
    List<PermissionType> getPermissions();

    /**
     * Is this command for server-use only?
     *
     * @return Is this command for server-use only?
     */
    boolean isServerOnly();

    /**
     * Gets all the shortcuts of the command.
     *
     * @return The shortcuts of the command.
     */
    List<String> getShortcuts();

}
