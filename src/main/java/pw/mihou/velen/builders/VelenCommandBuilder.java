package pw.mihou.velen.builders;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import pw.mihou.velen.impl.VelenCommandImpl;
import pw.mihou.velen.interfaces.*;
import pw.mihou.velen.interfaces.messages.types.VelenConditionalMessage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VelenCommandBuilder {

    private final List<Long> requiredRoles = new ArrayList<>();
    private final List<Long> requiredUsers = new ArrayList<>();
    private final List<PermissionType> permissions = new ArrayList<>();
    private final List<String> shortcuts = new ArrayList<>();
    private final List<Function<MessageCreateEvent, Boolean>> conditions = new ArrayList<>();
    private final List<Function<SlashCommandCreateEvent, Boolean>> conditionsSlash = new ArrayList<>();
    private final List<SlashCommandOption> options = new ArrayList<>();
    private final List<String> formats = new ArrayList<>();
    private VelenConditionalMessage conditionalMessage;
    private long serverId = 0L;
    private String category;
    private String name;
    private final List<String> usages = new ArrayList<>();
    private String description;
    private boolean defaultPermission = false;
    private VelenSlashEvent velenSlashEvent;
    private VelenHybridHandler velenHybridHandler;
    private Duration cooldown;
    private boolean serverOnly = false;
    private boolean privateOnly = false;
    private final List<String> middlewares = new ArrayList<>();
    private final List<String> afterwares = new ArrayList<>();
    private VelenEvent velenEvent;
    private Velen velen;

    /**
     * Sets the name of the command.
     *
     * @param commandName The name of the command.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder setName(String commandName) {
        this.name = commandName;
        return this;
    }

    /**
     * Sets whether to make this command a default permission or not.
     * This only functions with slash commands.
     *
     * @param defaultPermission The default permission to use.
     * @return VelenCommadnBuilder for chain calling methods.
     */
    public VelenCommandBuilder setDefaultPermission(boolean defaultPermission) {
        this.defaultPermission = defaultPermission;
        return this;
    }

    /**
     * Sets the usage of the command.
     * Deprecated for {@link VelenCommandBuilder#addUsage(String)}, still can be used
     * to add usages.
     *
     * @param usage The usage of the command.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    @Deprecated
    public VelenCommandBuilder setUsage(String usage) {
        return addUsage(usage);
    }

    /**
     * Adds a usage of the command.
     *
     * @param usage The usage of the command.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder addUsage(String usage) {
        this.usages.add(usage);
        return this;
    }

    /**
     * Adds multiple usages of the command.
     *
     * @param usages The usages of the command.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder addUsages(String... usages) {
        this.usages.addAll(Arrays.asList(usages));
        return this;
    }

    /**
     * Adds multiple or single formats of a command, this can be used to grab options
     * of a command with its name.
     *
     * @param formats The formats to add.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder addFormats(String... formats) {
        this.formats.addAll(Arrays.asList(formats));
        return this;
    }

    /**
     * Sets the description of the command.
     *
     * @param description The description of the command.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the category of the command.
     *
     * @param category The category of the command.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
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
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder setCooldown(Duration duration) {
        this.cooldown = duration;
        return this;
    }

    /**
     * Make this command only useable to a certain role.
     *
     * @param roleID The ID of the role that users must have to use the command.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder requireRole(long roleID) {
        requiredRoles.add(roleID);
        return this;
    }

    /**
     * Make this command only useable to certain roles.
     *
     * @param roles The roles that the users must have to use the command.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder requireRoles(long... roles) {
        Arrays.stream(roles).forEach(requiredRoles::add);
        return this;
    }

    /**
     * Make this command only useable to a user.
     *
     * @param user The user who can use this command.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder requireUser(long user) {
        requiredUsers.add(user);
        return this;
    }

    /**
     * Makes this command only useable to certain users.
     *
     * @param users The users who can use this command.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder requireUsers(long... users) {
        Arrays.stream(users).forEach(requiredUsers::add);
        return this;
    }

    /**
     * Makes this command require a certain permission.
     *
     * @param permissionType The permission to require.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder requirePermission(PermissionType permissionType) {
        permissions.add(permissionType);
        return this;
    }

    /**
     * Makes this command require the following permissions.
     *
     * @param perms The permissions to require.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
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
     * @return {@link VelenCommandBuilder} for chain-calling methods..
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
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder addShortcuts(String... shortcuts) {
        this.shortcuts.addAll(Arrays.asList(shortcuts));
        return this;
    }

    /**
     * Adds an option that will be used for slash commands.
     * <h3>This is only applicable for slash commands!</h3>
     *
     * @param option The option to add.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder addOption(SlashCommandOptionBuilder option) {
        options.add(option.build());
        return this;
    }

    /**
     * Adds multiple options that will be used for slash commands.
     * <h3>This is only applicable for slash commands!</h3>
     *
     * @param options The option to add.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder addOptions(SlashCommandOptionBuilder... options) {
        this.options.addAll(Arrays.stream(options).map(SlashCommandOptionBuilder::build).collect(Collectors.toList()));
        return this;
    }

    /**
     * Adds a condition that the user or event has to meet
     * before the command is triggered.
     * <h3>This is for message commands, {@link VelenCommandBuilder#addConditionForSlash(Function)} should be used
     * for slash commands.</h3>
     * <p>
     * An example would be if {@link VelenCommandBuilder#requireRole(long)} and
     * {@link VelenCommandBuilder#requirePermission(PermissionType)} is hardly enough
     * to lock the user, for example, cases where the user has to optionally meet either
     * instead all of them.
     *
     * @param condition The condition the event has to meet.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder addCondition(Function<MessageCreateEvent, Boolean> condition) {
        this.conditions.add(condition);
        return this;
    }

    /**
     * Adds a condition that the user or event has to meet
     * before the command is triggered.
     * <h3>This is for slash commands, {@link VelenCommandBuilder#addCondition(Function)} should be used
     * for message commands.</h3>
     * <p>
     * An example would be if {@link VelenCommandBuilder#requireRole(long)} and
     * {@link VelenCommandBuilder#requirePermission(PermissionType)} is hardly enough
     * to lock the user, for example, cases where the user has to optionally meet either
     * instead all of them.
     *
     * @param condition The condition the event has to meet.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder addConditionForSlash(Function<SlashCommandCreateEvent, Boolean> condition) {
        this.conditionsSlash.add(condition);
        return this;
    }

    /**
     * Adds a message to be sent to the user whenever the conditions on {@link VelenCommandBuilder#addConditionForSlash(Function)}
     * or {@link VelenCommandBuilder#addCondition(Function)} are not met. This is by defualt, null which means we don't send
     * a message to the user.
     *
     * @param message The message to be sent.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder setConditionalMessage(VelenConditionalMessage message) {
        this.conditionalMessage = message;
        return this;
    }

    /**
     * Adds an option that will be used for slash commands.
     * <h3>This is only applicable for slash commands!</h3>
     *
     * @param option The option to add.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder addOption(SlashCommandOption option) {
        options.add(option);
        return this;
    }

    /**
     * Adds multiple options that will be used for slash commands.
     * <h3>This is only applicable for slash commands!</h3>
     *
     * @param options The option to add.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder addOptions(SlashCommandOption... options) {
        this.options.addAll(Arrays.asList(options));
        return this;
    }

    /**
     * Sets the handler for when the command is invoked, <b>THIS IS REQUIRED TO SET</b>.
     * You must set this one as this one will be executed when the command is invoked by a user.
     *
     * @param event The Velen Event to use when the command is invoked.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder doEventOnInvocation(VelenEvent event) {
        this.velenEvent = event;
        return this;
    }

    /**
     * Sets the handler for when the command is invoked through slash command, <b>THIS IS REQUIRED TO SET whenever
     * you are using a slash command</b>.
     * You must set this one as this one will be executed when the command is invoked by a user.
     *
     * @param event The Velen Slash Event to use when the command is invoked.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder setSlashEvent(VelenSlashEvent event) {
        this.velenSlashEvent = event;
        return this;
    }

    /**
     * Sets the hybrid handler for when this command is invoked through either slash command or message command. This
     * will remove any previously set slash event or {@link VelenEvent} as a {@link VelenHybridHandler} is priority.
     *
     * @param handler The handler for the event.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder setHybridHandler(VelenHybridHandler handler) {
        this.velenHybridHandler = handler;
        this.velenSlashEvent = null;
        this.velenEvent = null;

        return this;
    }

    /**
     * Should this command be server only?
     * <h3> This is only for making the command work only for servers, please use
     * {@link VelenCommandBuilder#setServerOnly(boolean, long)} for slash commands!</h3>
     *
     * @param serverOnly Is this command a server only command?
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder setServerOnly(boolean serverOnly) {
        this.serverOnly = serverOnly;
        return this;
    }

    /**
     * Adds one or more middlewares to the list of middlewares to be used, ensure that
     * all the middlewares here are available in the {@link pw.mihou.velen.interfaces.Velen} instance.
     *
     * @param middlewares The middlewares to attach.
     * @return {@link VelenCommandBuilder} for chain-calling methods.
     */
    public VelenCommandBuilder addMiddlewares(String... middlewares) {
        this.middlewares.addAll(Arrays.asList(middlewares));
        return this;
    }

    /**
     * Adds one or more afterwares to the list of afterwares to be used, ensure that
     * all the middlewares here are available in the {@link pw.mihou.velen.interfaces.Velen} instance.
     *
     * @param afterwares The afterwares to attach.
     * @return {@link VelenCommandBuilder} for chain-calling methods.
     */
    public VelenCommandBuilder addAfterwares(String... afterwares) {
        this.afterwares.addAll(Arrays.asList(afterwares));
        return this;
    }

    /**
     * Should this command be private-channel only?
     *
     * @param privateChannelOnly Is this command a private channel only command?
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder setPrivateChannelOnly(boolean privateChannelOnly) {
        this.privateOnly = privateChannelOnly;
        return this;
    }

    /**
     * Should this slash command be for a server only?
     *
     * @param serverOnly Is this command a server only command?
     * @param serverId   The server ID to register the slash command on (this limits the slash
     *                   command to that server only).
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder setServerOnly(boolean serverOnly, long serverId) {
        this.serverOnly = serverOnly;
        this.serverId = serverId;
        return this;
    }

    /**
     * The {@link Velen} instance that is used to fetch important modules
     * of the framework, for instance, the rate-limiter.
     *
     * @param velen The {@link Velen} to use.
     * @return {@link VelenCommandBuilder} for chain-calling methods..
     */
    public VelenCommandBuilder setVelen(Velen velen) {
        this.velen = velen;
        return this;
    }

    /**
     * Registers the command onto the {@link Velen} instance which would then allow it
     * to function and start listening to commands.
     */
    public void attach() {
        if (velen == null)
            throw new IllegalArgumentException("A command requires a non-null instance of Velen @ https://github.com/ShindouMihou/Velen");

        velen.addCommand(build());
    }

    /**
     * Creates a Velen Command which you can then attach to Velen
     * or retrieve the values from.
     *
     * @return A velen command.
     */
    public VelenCommand build() {

        if (velen == null)
            throw new IllegalArgumentException("Velen cannot be null when creating a command!");

        if (velenHybridHandler == null && velenEvent == null && velenSlashEvent == null)
            throw new IllegalArgumentException("Failed to create command ["+name+"] with reasons:Velen Hybrid handler, Velen Message Handler or Velen Slash Handler cannot be null " +
                    "when creating a command since it will be executed when the command is triggered!");

        // Velen Hybrid Handler is always a priority over the others.
        if(velenHybridHandler != null && (velenEvent != null || velenSlashEvent != null)) {
            this.velenEvent = null;
            this.velenSlashEvent = null;
        }

        if (cooldown == null)
            cooldown = velen.getRatelimiter().getDefaultCooldown();

        if (description == null)
            description = "No description";

        if (category == null)
            category = "";

        VelenCommandImpl.GeneralCollective general = new VelenCommandImpl
                .GeneralCollective(name, description, shortcuts, category, cooldown, usages, defaultPermission);

        VelenCommandImpl.RequireCollective requires = new VelenCommandImpl
                .RequireCollective(requiredRoles, requiredUsers, permissions);

        VelenCommandImpl.ConditionalCollective conditional = new VelenCommandImpl
                .ConditionalCollective(conditions, conditionsSlash, conditionalMessage);

        VelenCommandImpl.Settings settings = new VelenCommandImpl
                .Settings(serverOnly, privateOnly, serverId, formats, options);

        VelenCommandImpl.Handlers handlers = new VelenCommandImpl
                .Handlers(velenEvent, velenSlashEvent, velenHybridHandler);

        VelenCommandImpl.Warehouse warehouse = new VelenCommandImpl.Warehouse(
                middlewares.stream().map(s -> velen.getMiddleware(s)
                                .orElseThrow(() -> new IllegalStateException("The middleware " + s + " couldn't be found.")))
                        .collect(Collectors.toList()),
                afterwares.stream().map(s -> velen.getAfterware(s)
                                .orElseThrow(() -> new IllegalStateException("The afterware " + s + " couldn't be found.")))
                        .collect(Collectors.toList())
        );

        return new VelenCommandImpl(general, requires, conditional, settings, handlers, warehouse, velen);

    }

}
