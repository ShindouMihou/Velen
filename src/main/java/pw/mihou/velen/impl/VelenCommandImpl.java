package pw.mihou.velen.impl;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.*;
import pw.mihou.velen.impl.commands.children.BaseInteractionCommand;
import pw.mihou.velen.impl.commands.children.BaseMessageCommand;
import pw.mihou.velen.interfaces.*;
import pw.mihou.velen.interfaces.messages.types.VelenConditionalMessage;
import pw.mihou.velen.interfaces.middleware.VelenMiddleware;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

public class VelenCommandImpl implements VelenCommand {

    private final Velen velen;
    private String stringValue;
    private final GeneralCollective general;
    private final RequireCollective requires;
    private final ConditionalCollective conditional;
    private final Settings settings;
    private final Handlers handlers;
    private final Warehouse warehouse;

    private final BaseMessageCommand baseMessageCommand = new BaseMessageCommand(this);
    private final BaseInteractionCommand baseInteractionCommand = new BaseInteractionCommand(this);

    public VelenCommandImpl(GeneralCollective general, RequireCollective requires, ConditionalCollective conditional,
                            Settings settings, Handlers handlers, Warehouse warehouse, Velen velen) {
        this.general = general;
        this.requires = requires;
        this.conditional = conditional;
        this.settings = settings;
        this.handlers = handlers;
        this.velen = velen;
        this.warehouse = warehouse;
    }

    /**
     * This handles the incoming command event from Discord
     * and handles the to-do tasks such as authentication and so forth.
     *
     * @param event The message event received.
     */
    public void onReceive(MessageCreateEvent event, String[] args) {
        baseMessageCommand.onReceive(event, args);
    }
    
    /**
     * This handles the incoming command event from Discord
     * and handles the to-do tasks such as authentication and so forth.
     *
     * @param event The message event received.
     */
    public void onReceive(SlashCommandCreateEvent event) {
        baseInteractionCommand.onReceive(event);
    }

    @Override
    public List<SlashCommandOption> getOptions() {
        return settings.options == null ? Collections.emptyList() : settings.options;
    }

    /**
     * Retrieves all the possible formats of this command.
     *
     * @return The possible formats.
     */
    public List<String> getFormats() {
        return settings.commandFormats;
    }

    @Override
    public boolean isSlashCommandOnly() {
        return handlers.velenEvent == null && handlers.hybridHandler == null && handlers.velenSlashEvent != null;
    }

    @Override
    public boolean isPrivateOnly() {
        return settings.privateOnly;
    }

    @Override
    public long getServerId() {
        return settings.serverId;
    }

    @Override
    public String getName() {
        return general.name;
    }

    @Override
    public String getDescription() {
        return general.description;
    }

    @Override
    public Duration getCooldown() {
        return general.cooldown;
    }

    @Override
    public List<String> getUsages() {
        return general.usage;
    }

    @Override
    public String getUsage() {
        return general.usage.get(0);
    }

    @Override
    public List<Long> getRequiredRoles() {
        return requires.requiredRoles;
    }

    @Override
    public List<Long> getRequiredUsers() {
        return requires.requiredUsers;
    }

    @Override
    public List<PermissionType> getPermissions() {
        return requires.permissions;
    }

    @Override
    public boolean isServerOnly() {
        return settings.serverOnly;
    }

    @Override
    public boolean supportsSlashCommand() {
        return handlers.velenSlashEvent != null || handlers.hybridHandler != null;
    }

    @Override
    public boolean isHybrid() {
        return handlers.hybridHandler != null || (handlers.velenSlashEvent != null && handlers.velenEvent != null);
    }

    @Override
    public boolean isDefaultPermissionEnabled() {
        return general.defaultPermission;
    }

    @Override
    public String[] getShortcuts() {
        return general.shortcuts;
    }

    @Override
    public String getCategory() {
        return general.category;
    }

    @Override
    public List<VelenMiddleware> getMiddlewares() {
        return warehouse.getMiddlewares();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VelenCommandImpl that = (VelenCommandImpl) o;
        return getVelen().equals(that.getVelen())
                && Objects.equals(stringValue, that.stringValue)
                && general.equals(that.general)
                && requires.equals(that.requires)
                && conditional.equals(that.conditional)
                && settings.equals(that.settings)
                && handlers.equals(that.handlers)
                && Objects.equals(baseMessageCommand, that.baseMessageCommand)
                && Objects.equals(baseInteractionCommand, that.baseInteractionCommand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVelen(), stringValue, general,
                requires, conditional, settings, handlers,
                baseMessageCommand, baseInteractionCommand);
    }

    @Override
    public String toString() {
        if (stringValue != null && !stringValue.isEmpty())
            return stringValue;

        stringValue = general.name +
                " (Description: " + general.description +
                ", Cooldown: " + general.cooldown.toMillis() +
                ", Slash: " + supportsSlashCommand() +
                ", Hybrid: " + (!isSlashCommandOnly() && supportsSlashCommand()) +
                ")";

        return stringValue;
    }

    /**
     * Retrieves the developer-defined additional restraints for
     * the command.
     *
     * @return The additional restraints.
     */
    public List<Function<MessageCreateEvent, Boolean>> getMessageConditions() {
        return conditional.conditions;
    }

    /**
     * Retrieves the developer-defined additional restraints for
     * the command.
     *
     * @return The additional restraints.
     */
    public List<Function<SlashCommandCreateEvent, Boolean>> getSlashConditions() {
        return conditional.conditionsSlash;
    }

    /**
     * Retrieves the conditional message for this command.
     *
     * @return The conditional message to use.
     */
    public VelenConditionalMessage getConditionalMessage() {
        return conditional.conditionalMessage;
    }

    /**
     * Retrieves the handler for interaction commands.
     * @return The handler for interaction commands.
     */
    public VelenSlashEvent getInteractionHandler() {
        return handlers.velenSlashEvent;
    }

    /**
     * Retrieves the handler for message commands.
     * @return The handler for message commands.
     */
    public VelenEvent getMessageHandler() {
        return handlers.velenEvent;
    }

    /**
     * Retrieves the handler for hybrid commands.
     * @return The handler for hybrid commands.
     */
    public VelenHybridHandler getHybridHandler() {
        return handlers.hybridHandler;
    }

    /**
     * Retrieves the Velen instance this command was
     * registered on.
     *
     * @return The Velen instance used to register this command.
     */
    public Velen getVelen() {
        return velen;
    }


    public static class Warehouse {
        private final List<VelenMiddleware> middlewares;

        /**
         * Creates a brand new warehouse that can store
         * middlewares and afterwares.
         *
         * @param middlewares The middlewares to store.
         */
        public Warehouse(List<VelenMiddleware> middlewares) {
            this.middlewares = middlewares;
        }

        /**
         * Retrieves all the middlewares of this command.
         *
         * @return All the middlewares being used in this command.
         */
        public List<VelenMiddleware> getMiddlewares() {
            return middlewares;
        }

    }

    public static class RequireCollective {
        public final List<Long> requiredRoles;
        public final List<Long> requiredUsers;
        public final List<PermissionType> permissions;

        /**
         * Creates a new required collective which holds the
         * required roles, users and permissions for the command.
         *
         * @param requiredRoles The required roles of the command.
         * @param requiredUsers The required users of the command.
         * @param permissions The required permissions of the command.
         */
        public RequireCollective(List<Long> requiredRoles,
                                 List<Long> requiredUsers,
                                 List<PermissionType> permissions) {
            this.requiredRoles = requiredRoles;
            this.requiredUsers = requiredUsers;
            this.permissions = permissions;
        }
    }

    public static class GeneralCollective {
        public final String name;
        public final String description;
        public final String category;
        public final Duration cooldown;
        public final String[] shortcuts;
        public final List<String> usage;
        public final boolean defaultPermission;

        /**
         * Creates a general collective that contains the general information
         * of the command from the name, etc.
         *
         * @param name The name of the command.
         * @param description The description of the command.
         * @param shortcuts The shortcuts of the command.
         * @param category The category of the command.
         * @param cooldown The cooldown of the command.
         * @param usage The usages of the command.
         * @param defaultPermission Should this command only be useable for admins and guild owners?
         */
        public GeneralCollective(String name, String description, List<String> shortcuts,
                                 String category, Duration cooldown, List<String> usage, boolean defaultPermission) {
            this.name = name;
            this.description = description;
            this.shortcuts = shortcuts.toArray(new String[0]);
            this.category = category;
            this.cooldown = cooldown;
            this.usage = usage;
            this.defaultPermission = defaultPermission;
        }
    }

    public static class ConditionalCollective {
        public final List<Function<MessageCreateEvent, Boolean>> conditions;
        public final List<Function<SlashCommandCreateEvent, Boolean>> conditionsSlash;
        public final VelenConditionalMessage conditionalMessage;

        /**
         * Creates a new conditional collective that contains the developer-defined conditions
         * needed to be fulfilled.
         *
         * @param conditions The conditions for mesasges commands.
         * @param conditionsSlash The conditions for slash  commands.
         * @param conditionalMessage The conditional message.
         */
        public ConditionalCollective(List<Function<MessageCreateEvent, Boolean>> conditions,
                                     List<Function<SlashCommandCreateEvent, Boolean>> conditionsSlash,
                                     VelenConditionalMessage conditionalMessage) {
            this.conditions = conditions;
            this.conditionsSlash = conditionsSlash;
            this.conditionalMessage = conditionalMessage;
        }
    }

    public static class Settings {
        public final boolean serverOnly;
        public final boolean privateOnly;

        public final long serverId;
        public final List<String> commandFormats;

        public final List<SlashCommandOption> options;

        /**
         * Creates the settings for the command.
         *
         * @param serverOnly Is this command for servers only?
         * @param privateOnly Is this command for private channels only?
         * @param serverId The server ID to limit this command towards.
         * @param commandFormats The message formats to use for this command.
         * @param options The slash commands options for this command.
         */
        public Settings(boolean serverOnly, boolean privateOnly, long serverId, List<String> commandFormats,
                        List<SlashCommandOption> options) {
            this.serverOnly = serverOnly;
            this.privateOnly = privateOnly;
            this.serverId = serverId;
            this.commandFormats = commandFormats;
            this.options = options;
        }
    }

    public static class Handlers {
        public final VelenEvent velenEvent;
        public final VelenSlashEvent velenSlashEvent;
        public final VelenHybridHandler hybridHandler;

        /**
         * Creates a new handler storage that holds all the possible handlers
         * for this command.
         *
         * @param velenEvent The message command handler.
         * @param velenSlashEvent The slash command handler.
         * @param hybridHandler The hybrid command handler.
         */
        public Handlers(VelenEvent velenEvent, VelenSlashEvent velenSlashEvent, VelenHybridHandler hybridHandler) {
            this.velenEvent = velenEvent;
            this.velenSlashEvent = velenSlashEvent;
            this.hybridHandler = hybridHandler;
        }
    }
}
