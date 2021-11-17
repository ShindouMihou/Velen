package pw.mihou.velen.impl.commands;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import pw.mihou.velen.impl.VelenCommandImpl;
import pw.mihou.velen.interfaces.VelenArguments;
import pw.mihou.velen.interfaces.afterware.types.VelenHybridAfterware;
import pw.mihou.velen.interfaces.afterware.types.VelenMessageAfterware;
import pw.mihou.velen.interfaces.afterware.types.VelenSlashAfterware;
import pw.mihou.velen.interfaces.hybrid.event.VelenGeneralEvent;
import pw.mihou.velen.interfaces.hybrid.event.internal.VelenGeneralEventImpl;
import pw.mihou.velen.interfaces.middleware.VelenGate;
import pw.mihou.velen.interfaces.middleware.types.VelenHybridMiddleware;
import pw.mihou.velen.interfaces.middleware.types.VelenMessageMiddleware;
import pw.mihou.velen.interfaces.middleware.types.VelenSlashMiddleware;
import pw.mihou.velen.interfaces.routed.VelenRoutedOptions;
import pw.mihou.velen.ratelimiter.entities.RatelimitEntity;
import pw.mihou.velen.utils.Pair;
import pw.mihou.velen.utils.VelenThreadPool;

import java.util.ArrayList;
import java.util.List;
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

        if (!instance.getRequiredUsers().isEmpty() && !instance.getRequiredUsers()
                .contains(event.getInteraction().getUser().getId()))
            return false;

        return true;
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

        if (!instance.getRequiredUsers().isEmpty() && !instance.getRequiredUsers()
                .contains(event.getMessageAuthor().getId()))
            return false;

        return true;
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
     * Applies all the middleware restraints for a hybrid command.
     *
     * @param middlewares The middlewares to use.
     * @param event The event to use.
     * @return Is this accepted by the gate?
     */
    public Pair<Boolean, String> applyHybridMiddlewares(List<VelenHybridMiddleware> middlewares, VelenGeneralEvent event) {
        for (VelenHybridMiddleware middleware : middlewares) {
            Pair<Boolean, String> gate = middleware.onEvent(event, event.getArguments(), event.getCommand(), new VelenGate());

            if (!gate.getLeft())
                return gate;
        }

        return Pair.of(true, null);
    }

    /**
     * Applies all the middleware restraints for a hybrid command.
     *
     * @param middlewares The middlewares to use.
     * @param event The event to use.
     * @return Is this accepted by the gate?
     */
    public Pair<Boolean, String> applySlashMiddlewares(List<VelenSlashMiddleware> middlewares, SlashCommandCreateEvent event) {
        for (VelenSlashMiddleware middleware : middlewares) {
            Pair<Boolean, String> gate = middleware.onEvent(event, instance, new VelenGate());

            if (!gate.getLeft())
                return gate;
        }

        return Pair.of(true, null);
    }

    /**
     * Applies all the middleware restraints for a hybrid command.
     *
     * @param middlewares The middlewares to use.
     * @param event The event to use.
     * @return Is this accepted by the gate?
     */
    public Pair<Boolean, String> applyMessageMiddlewares(List<VelenMessageMiddleware> middlewares, MessageCreateEvent event, VelenRoutedOptions options) {
        for (VelenMessageMiddleware middleware : middlewares) {
            Pair<Boolean, String> gate = middleware.onEvent(event, instance, options, new VelenGate());

            if (!gate.getLeft())
                return gate;
        }

        return Pair.of(true, null);
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
     * Gets all the hybrid middlewares of this command.
     *
     * @return All the hybrid middlewares for this command.
     */
    public List<VelenHybridMiddleware> getHybridMiddlewares() {
        List<VelenHybridMiddleware> middlewares = new ArrayList<>();

        if (instance.getVelen().findCategory(instance.getCategory()) != null) {
            middlewares.addAll(instance.getVelen().findCategory(instance.getCategory()).getHybridMiddlewares());
        }

        middlewares.addAll(instance.getHybridMiddlewares());
        return middlewares;
    }

    /**
     * Gets all the message middlewares of this command.
     *
     * @return All the message middlewares for this command.
     */
    public List<VelenMessageMiddleware> getMessageMiddlewares() {
        List<VelenMessageMiddleware> middlewares = new ArrayList<>();

        if (instance.getVelen().findCategory(instance.getCategory()) != null) {
            middlewares.addAll(instance.getVelen().findCategory(instance.getCategory()).getMessageMiddlewares());
        }

        middlewares.addAll(instance.getMessageMiddlewares());
        return middlewares;
    }


    /**
     * Gets all the slash middlewares of this command.
     *
     * @return All the slash middlewares for this command.
     */
    public List<VelenSlashMiddleware> getSlashMiddlewares() {
        List<VelenSlashMiddleware> middlewares = new ArrayList<>();

        if (instance.getVelen().findCategory(instance.getCategory()) != null) {
            middlewares.addAll(instance.getVelen().findCategory(instance.getCategory()).getSlashMiddlewares());
        }

        middlewares.addAll(instance.getSlashMiddlewares());
        return middlewares;
    }

    /**
     * Gets all the slash afterwares of this command.
     *
     * @return All the slash afterwares for this command.
     */
    public List<VelenSlashAfterware> getSlashAfterwares() {
        List<VelenSlashAfterware> afterwares = new ArrayList<>();

        if (instance.getVelen().findCategory(instance.getCategory()) != null) {
            afterwares.addAll(instance.getVelen().findCategory(instance.getCategory()).getSlashAfterwares());
        }

        afterwares.addAll(instance.getSlashAfterwares());
        return afterwares;
    }

    /**
     * Gets all the message afterwares of this command.
     *
     * @return All the message afterwares for this command.
     */
    public List<VelenMessageAfterware> getMessageAfterwares() {
        List<VelenMessageAfterware> afterwares = new ArrayList<>();

        if (instance.getVelen().findCategory(instance.getCategory()) != null) {
            afterwares.addAll(instance.getVelen().findCategory(instance.getCategory()).getMessageAfterwares());
        }

        afterwares.addAll(instance.getMessageAfterwares());
        return afterwares;
    }

    /**
     * Gets all the hybrid afterwares of this command.
     *
     * @return All the hybrid afterwares for this command.
     */
    public List<VelenHybridAfterware> getHybridAfterwares() {
        List<VelenHybridAfterware> afterwares = new ArrayList<>();

        if (instance.getVelen().findCategory(instance.getCategory()) != null) {
            afterwares.addAll(instance.getVelen().findCategory(instance.getCategory()).getHybridAfterwares());
        }

        afterwares.addAll(instance.getHybridAfterwares());
        return afterwares;
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
            Pair<Boolean, String> middlewareResponse = applySlashMiddlewares(
                    getSlashMiddlewares(),
                    event
            );

            if (!middlewareResponse.getLeft()) {
                if (middlewareResponse.getRight() != null) {
                    event.getSlashCommandInteraction()
                            .createImmediateResponder()
                            .setContent(middlewareResponse.getRight())
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond();
                }
                return;
            }

            instance.getInteractionHandler()
                    .onEvent(event,
                            event.getSlashCommandInteraction(),
                            event.getSlashCommandInteraction().getUser(),
                            new VelenArguments(event.getSlashCommandInteraction().getOptions()),
                            event.getSlashCommandInteraction().getOptions(),
                            event.getSlashCommandInteraction().createImmediateResponder());
            // Execute the afterwares.
            getSlashAfterwares().forEach(afterware -> VelenThreadPool.executorService
                    .submit(() -> afterware.afterEvent(event, instance)));
        } else {
            VelenGeneralEvent e = new VelenGeneralEventImpl(instance.getName(), event, null, null, instance);

            Pair<Boolean, String> middlewareResponse = applyHybridMiddlewares(
                    getHybridMiddlewares(),
                    e
            );

            if (!middlewareResponse.getLeft()) {
                if (middlewareResponse.getRight() != null) {
                    e.createResponder()
                            .setContent(middlewareResponse.getRight())
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond();
                }
                return;
            }

            instance.getHybridHandler().onEvent(e, e.createResponder(), e.getUser(), e.getArguments());
            // Execute the afterwares.
            getHybridAfterwares().forEach(afterware -> VelenThreadPool.executorService
                    .submit(() -> afterware.afterEvent(e, e.getArguments(), instance)));
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

                VelenRoutedOptions options = new VelenRoutedOptions(instance, event);
                Pair<Boolean, String> middlewareResponse = applyMessageMiddlewares(
                        getMessageMiddlewares(),
                        event,
                        options
                );

                if (!middlewareResponse.getLeft()) {
                    if (middlewareResponse.getRight() != null) {
                        event.getMessage().reply(middlewareResponse.getRight());
                    }
                    return;
                }

                instance.getMessageHandler().onEvent(event, event.getMessage(), u, args, options);
                // Execute the afterwares.
                getMessageAfterwares().forEach(afterware -> VelenThreadPool.executorService
                        .submit(() -> afterware.afterEvent(event, instance, options)));
            } else {
                VelenGeneralEvent e = new VelenGeneralEventImpl(instance.getName(), null, event, args, instance);

                Pair<Boolean, String> middlewareResponse = applyHybridMiddlewares(
                        getHybridMiddlewares(),
                        e
                );

                if (!middlewareResponse.getLeft()) {
                    if (middlewareResponse.getRight() != null) {
                        e.createResponder()
                                .setContent(middlewareResponse.getRight())
                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                .respond();
                    }
                    return;
                }

                instance.getHybridHandler().onEvent(e, e.createResponder(), e.getUser(), e.getArguments());
                // Execute the afterwares.
                getHybridAfterwares().forEach(afterware -> VelenThreadPool.executorService
                        .submit(() -> afterware.afterEvent(e, e.getArguments(), instance)));
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
