package pw.mihou.velen.impl.commands;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.impl.VelenCommandImpl;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.hybrid.event.VelenGeneralEvent;
import pw.mihou.velen.interfaces.hybrid.event.internal.VelenGeneralEventImpl;
import pw.mihou.velen.interfaces.routed.VelenRoutedOptions;
import pw.mihou.velen.ratelimiter.entities.RatelimitEntity;

import java.util.Objects;
import java.util.function.Consumer;

public class BaseCommandImplementation {

    public final VelenCommandImpl instance;

    /**
     * Creates a new Base Command instance that requires
     * the host VelenCommand instance.
     *
     * @param instance The instance to use.
     */
    public BaseCommandImplementation(VelenCommandImpl instance) {
        this.instance = instance;
    }

    /**
     * Checks whether the user can use this command or not via
     * the required permissions.
     *
     * @param server The server instance to use, nullable.
     * @param user The user instance to use.
     * @return Can the user use this command?
     */
    public boolean applyPermissionRestraint(Server server, User user) {
        return server == null || instance.getPermissions().isEmpty() || server.getPermissions(user)
                .getAllowedPermission().containsAll(instance.getPermissions());
    }

    /**
     * Applies the rate-limiter for Velen.
     *
     * @param user The user to rate-limit.
     * @param server The server where the command was executed.
     * @param onLimited If the user is rate-limited.
     * @param onSuccess If the command can be executed.
     */
    public void applyRatelimiter(long user, long server, Consumer<Long> onLimited, Consumer<RatelimitEntity> onSuccess) {
        instance.getVelen().getRatelimiter()
                .ratelimit(user, server, instance.toString(), onLimited, onSuccess);
    }

    /**
     * Applies general restraints for slash commands.
     *
     * @param event The event instance to use.
     * @return Can the user use this command?
     */
    public boolean applyRestraints(SlashCommandCreateEvent event) {
        if (instance.isServerOnly() && !event.getSlashCommandInteraction().getServer().isPresent())
            return false;

        if (instance.isPrivateOnly() && event.getSlashCommandInteraction().getServer().isPresent())
            return false;

        // We need this condition in case Discord decides to go through with their
        // optional channel thing which sounds odd.
        if (!event.getSlashCommandInteraction().getChannel().isPresent())
            throw new IllegalStateException("The channel is somehow not present; this is possibly a change in Discord's side " +
                    "and may need to be addressed, please send an issue @ https://github.com/ShindouMihou/Velen");

        return !instance.getRequiredUsers().isEmpty() && !instance.getRequiredUsers()
                .contains(event.getInteraction().getUser().getId());
    }


    /**
     * Applies general restraints for message commands.
     *
     * @param event The event instance to use.
     * @return Can the user use this command?
     */
    public boolean applyRestraints(MessageCreateEvent event) {
        if (instance.isServerOnly() && !event.getServer().isPresent())
            return false;

        if (instance.isPrivateOnly() && event.getServer().isPresent())
            return false;

        return !instance.getRequiredUsers().isEmpty() && !instance.getRequiredUsers()
                .contains(event.getMessageAuthor().getId());
    }

    /**
     * Checks whether the user can use this command or not via the
     * required roles.
     *
     * @param server The server instance to use, nullable.
     * @param user The user instance to use.
     * @return Can the user use this command?
     */
    public boolean applyRoleRestraints(Server server, User user) {
        return server == null || instance.getRequiredRoles().isEmpty() || instance.getRequiredRoles()
                .stream()
                .anyMatch(aLong -> server.getRoles(user)
                        .stream()
                        .map(Role::getId)
                        .anyMatch(r -> Objects.equals(aLong, r)
                        )
                );
    }

    /**
     * Applies all the restraint requirements for the command.
     *
     * @param event The event instance to use.
     * @return Can the user use this command?
     */
    public boolean applyMessageRestraints(MessageCreateEvent event) {
        return instance.getMessageConditions().isEmpty() || instance.getMessageConditions()
                .stream().allMatch(function -> function.apply(event));
    }

    /**
     * Applies all the restraint requirements for the command.
     *
     * @param event The event instance to use.
     * @return Can the user use this command?
     */
    public boolean applySlashRestraints(SlashCommandCreateEvent event) {
        return instance.getSlashConditions().isEmpty() || instance.getSlashConditions()
                .stream().allMatch(function -> function.apply(event));
    }

    /**
     * Dispatches an event for slash commands.
     *
     * @param event The event to dispatch.
     */
    public void dispatch(SlashCommandCreateEvent event) {
        if (instance.getHybridHandler() == null && instance.getInteractionHandler() == null)
            throw new IllegalStateException("There is no slash handler found for the command " +
                    instance.getName() + ".");

        if (instance.getHybridHandler() == null) {
            instance.getInteractionHandler()
                    .onEvent(event,
                            event.getSlashCommandInteraction(),
                            event.getSlashCommandInteraction().getUser(),
                            new VelenArguments(event.getSlashCommandInteraction().getOptions()),
                            event.getSlashCommandInteraction().getOptions(),
                            event.getSlashCommandInteraction().createImmediateResponder());
        } else {
            VelenGeneralEvent e = new VelenGeneralEventImpl(instance.getName(), event, null, null, instance);
            instance.getHybridHandler().onEvent(e, e.createResponder(), e.getUser(), e.getArguments());
        }
    }

    /**
     * Dispatches an event for message commands.
     *
     * @param event The event to dispatch.
     * @param args The arguments provided from the initial dispatch.
     */
    public void dispatch(MessageCreateEvent event, String[] args) {
        event.getMessageAuthor().asUser().ifPresent(u -> {
            if (instance.getHybridHandler() == null && instance.getMessageHandler() == null)
                throw new IllegalStateException("There is no message handler found for the command " +
                        instance.getName() + ".");

            if (instance.getHybridHandler() == null) {
                instance.getMessageHandler().onEvent(event, event.getMessage(), u, args, new VelenRoutedOptions(instance, event));
            } else {
                VelenGeneralEvent e = new VelenGeneralEventImpl(instance.getName(), null, event, args, instance);
                instance.getHybridHandler().onEvent(e, e.createResponder(), e.getUser(), e.getArguments());
            }
        });
    }

    /**
     * Transforms an ordinary long id into Discord's readable
     * format for roles.
     *
     * @param id The ID to use.
     * @return A role format.
     */
    public String toRoleFormat(long id) {
        return "<@&"+id+">";
    }

}
