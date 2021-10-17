package pw.mihou.velen.interfaces;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
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
     * the required parameters filled.
     * <h3>This creates a hybrid between slash command and message command, which means
     * it will listen into both message commands and slash commands.</h3>
     *
     * @param name        The name of the command.
     * @param description The description of the command.
     * @param event       The event to execute when the command is invoked.
     * @param slashEvent  The slash event handler.
     * @param velen       The velen instance (which will be used to grab default rate-limiter, etc).
     * @return A VelenCommandBuilder in case you want to add more options.
     */
    static VelenCommandBuilder ofHybrid(String name, String description, Velen velen, VelenEvent event, VelenSlashEvent slashEvent) {
        return new VelenCommandBuilder()
                .setName(name)
                .doEventOnInvocation(event)
                .setVelen(velen)
                .setDescription(description)
                .setSlashEvent(slashEvent);
    }

    /**
     * Creates a new Velen command builder with all
     * the required parameters filled.
     * <h3>This creates a hybrid between slash command and message command, which means
     * it will listen into both message commands and slash commands.</h3>
     *
     * @param name        The name of the command.
     * @param description The description of the command.
     * @param handler     The handler to handle this event.
     * @param velen       The velen instance (which will be used to grab default rate-limiter, etc).
     * @return A VelenCommandBuilder in case you want to add more options.
     */
    static VelenCommandBuilder ofHybrid(String name, String description, Velen velen, VelenHybridHandler handler) {
        return new VelenCommandBuilder()
                .setName(name)
                .setHybridHandler(handler)
                .setVelen(velen)
                .setDescription(description);
    }

    /**
     * Creates a new Velen command builder with all
     * the required parameters filled.
     * <h3>This creates a hybrid between slash command and message command, which means
     * it will listen into both message commands and slash commands.</h3>
     *
     * @param name        The name of the command.
     * @param description The description of the command.
     * @param handler     The handler to handle this event.
     * @param velen       The velen instance (which will be used to grab default rate-limiter, etc).
     * @param options     The options to add to the command.
     * @return A VelenCommandBuilder in case you want to add more options.
     */
    static VelenCommandBuilder ofHybrid(String name, String description, Velen velen, VelenHybridHandler handler,
                                        SlashCommandOptionBuilder... options) {
        return new VelenCommandBuilder()
                .setName(name)
                .setHybridHandler(handler)
                .setVelen(velen)
                .setDescription(description)
                .addOptions(options);
    }

    /**
     * Creates a new Velen command builder with all
     * the required parameters filled.
     * <h3>This creates a hybrid between slash command and message command, which means
     * it will listen into both message commands and slash commands.</h3>
     *
     * @param name        The name of the command.
     * @param description The description of the command.
     * @param handler     The handler to handle this event.
     * @param velen       The velen instance (which will be used to grab default rate-limiter, etc).
     * @param options     The options to add to the command.
     * @return A VelenCommandBuilder in case you want to add more options.
     */
    static VelenCommandBuilder ofHybrid(String name, String description, Velen velen, VelenHybridHandler handler,
                                        SlashCommandOption... options) {
        return new VelenCommandBuilder()
                .setName(name)
                .setHybridHandler(handler)
                .setVelen(velen)
                .setDescription(description)
                .addOptions(options);
    }

    /**
     * Creates a new Velen command builder with all
     * the required parameters filled.
     * <h3>This creates a hybrid between slash command and message command, which means
     * it will listen into both message commands and slash commands.</h3>
     *
     * @param name        The name of the command.
     * @param description The description of the command.
     * @param event       The event to execute when the command is invoked.
     * @param slashEvent  The slash event handler.
     * @param options     The options to add to the command.
     * @param velen       The velen instance (which will be used to grab default rate-limiter, etc).
     * @return A VelenCommandBuilder in case you want to add more options.
     */
    static VelenCommandBuilder ofHybrid(String name, String description, Velen velen, VelenEvent event, VelenSlashEvent slashEvent,
                                        SlashCommandOptionBuilder... options) {
        return new VelenCommandBuilder()
                .setName(name)
                .doEventOnInvocation(event)
                .setVelen(velen)
                .addOptions(options)
                .setDescription(description)
                .setSlashEvent(slashEvent);
    }

    /**
     * Creates a new Velen command builder with all
     * the required parameters filled.
     * <h3>This creates a hybrid between slash command and message command, which means
     * it will listen into both message commands and slash commands.</h3>
     *
     * @param name        The name of the command.
     * @param description The description of the command.
     * @param event       The event to execute when the command is invoked.
     * @param slashEvent  The slash event handler.
     * @param options     The options to add to the command.
     * @param velen       The velen instance (which will be used to grab default rate-limiter, etc).
     * @return A VelenCommandBuilder in case you want to add more options.
     */
    static VelenCommandBuilder ofHybrid(String name, String description, Velen velen, VelenEvent event, VelenSlashEvent slashEvent,
                                        SlashCommandOption... options) {
        return new VelenCommandBuilder()
                .setName(name)
                .doEventOnInvocation(event)
                .setVelen(velen)
                .addOptions(options)
                .setDescription(description)
                .setSlashEvent(slashEvent);
    }

    /**
     * Creates a new Velen command builder with all
     * the required parameters filled.
     * <h3>This creates a slash command only.</h3>
     *
     * @param name        The name of the command.
     * @param description The description of the command.
     * @param slashEvent  The slash event handler.
     * @param options     The options to add to the command.
     * @param velen       The velen instance (which will be used to grab default rate-limiter, etc).
     * @return A VelenCommandBuilder in case you want to add more options.
     */
    static VelenCommandBuilder ofSlash(String name, String description, Velen velen, VelenSlashEvent slashEvent, SlashCommandOptionBuilder... options) {
        return new VelenCommandBuilder()
                .setName(name)
                .setVelen(velen)
                .addOptions(options)
                .setDescription(description)
                .setSlashEvent(slashEvent);
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
     * Gets the first usage of the command.
     *
     * @return The first usage of the command.
     */
    String getUsage();

    /**
     * Gets all the usages of the command.
     *
     * @return The usages of the command.
     */
    List<String> getUsages();

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
     * This is used to check if the command is slash command only.
     *
     * @return Is this command for slash command only?
     */
    boolean isSlashCommandOnly();

    /**
     * Gets all the slash command options for this command, this will return
     * an empty list if there are none or if the command is not of slash command.
     *
     * @return The slash command options for this command.
     */
    List<SlashCommandOption> getOptions();

    /**
     * Does this command support slash command?
     *
     * @return Does this command support slash command or not,
     * if it does then it could potentially be a hybrid command
     * as well.
     */
    boolean supportsSlashCommand();

    /**
     * This is used to check if the command is a hybrid command.
     *
     * @return Is this command a hybrid command?
     */
    boolean isHybrid();

    /**
     * Gets all the shortcuts of the command.
     *
     * @return The shortcuts of the command.
     */
    String[] getShortcuts();

    /**
     * Gets the category of the command.
     *
     * @return The category of the command.
     */
    String getCategory();

}
