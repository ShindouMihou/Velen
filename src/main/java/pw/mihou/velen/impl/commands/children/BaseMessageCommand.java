package pw.mihou.velen.impl.commands.children;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.velen.impl.VelenCommandImpl;
import pw.mihou.velen.impl.commands.BaseCommandImplementation;
import pw.mihou.velen.interfaces.messages.VelenOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.surface.embed.VelenConditionalEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.embed.VelenPermissionEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.embed.VelenRatelimitEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.embed.VelenRoleEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenConditionalOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenPermissionOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenRatelimitOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenRoleOrdinaryMessage;
import pw.mihou.velen.utils.VelenThreadPool;
import pw.mihou.velen.utils.VelenUtils;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseMessageCommand extends BaseCommandImplementation {

    /**
     * Creates a new Base Message Command implementation
     * that can handle the implementation of Message commands.
     *
     * @param instance The instance to use.
     */
    public BaseMessageCommand(VelenCommandImpl instance) {
        super(instance);
    }

    /**
     * This handles the incoming command event from Discord
     * and handles the to-do tasks such as authentication and so forth.
     *
     * @param event The message event received.
     * @param args The arguments received from the event.
     */
    public void onReceive(MessageCreateEvent event, String[] args) {
        if (!event.getMessageAuthor().asUser().isPresent())
            return;

        User user = event.getMessageAuthor().asUser().get();

        if (!applyRestraints(event)) {
            return;
        }

        if (!applyMessageRestraints(event)) {
            if (instance.getConditionalMessage() != null) {
                MessageBuilder builder = new MessageBuilder()
                        .replyTo(event.getMessage());

                if (instance.getConditionalMessage() instanceof VelenOrdinaryMessage) {
                    builder.setContent(((VelenConditionalOrdinaryMessage) instance.getConditionalMessage())
                            .load(user, event.getChannel(), instance.getName()));
                } else {
                    builder.addEmbed(((VelenConditionalEmbedMessage) instance.getConditionalMessage())
                            .load(user, event.getChannel(), instance.getName()));
                }

                builder.send(event.getChannel()).exceptionally(ExceptionLogger.get());
            }
            return;
        }

        if (!applyRoleRestraints(event.getServer().orElse(null), user)) {
            MessageBuilder builder = new MessageBuilder()
                    .setAllowedMentions(VelenUtils.createNoMentions())
                    .replyTo(event.getMessage());

            String roles = instance.getRequiredRoles().stream().map(this::toRoleFormat).collect(Collectors.joining(", "));

            if (instance.getVelen().getNoRoleMessage() instanceof VelenOrdinaryMessage) {
                builder.setContent(((VelenRoleOrdinaryMessage) instance.getVelen().getNoRoleMessage())
                        .load(roles, user, event.getChannel(), instance.getName()));
            } else {
                builder.addEmbed(((VelenRoleEmbedMessage) instance.getVelen().getNoRoleMessage())
                        .load(roles, user, event.getChannel(), instance.getName()));
            }

            builder.send(event.getChannel()).exceptionally(ExceptionLogger.get());
            return;
        }

        if (!applyPermissionRestraint(event.getServer().orElse(null), user)) {
            MessageBuilder builder = new MessageBuilder().replyTo(event.getMessage());

            if (instance.getVelen().getNoPermissionMessage() instanceof VelenOrdinaryMessage) {
                builder.setContent(((VelenPermissionOrdinaryMessage) instance.getVelen().getNoPermissionMessage())
                        .load(instance.getPermissions(), user, event.getChannel(), instance.getName()));
            } else {
                builder.addEmbed(((VelenPermissionEmbedMessage) instance.getVelen().getNoPermissionMessage())
                        .load(instance.getPermissions(), user, event.getChannel(), instance.getName()));
            }

            builder.send(event.getChannel()).exceptionally(ExceptionLogger.get());
            return;
        }

        if (instance.getCooldown() != null && !(instance.getCooldown().isZero() || instance.getCooldown().isNegative())) {
            long lock = event.getServer().map(Server::getId).orElse(user.getId());
            applyRatelimiter(user.getId(), lock,
                    remaining -> {
                        if (remaining > 0) {
                            MessageBuilder builder = new MessageBuilder().replyTo(event.getMessage());

                            if (instance.getVelen().getRatelimitedMessage() instanceof VelenOrdinaryMessage) {
                                builder.setContent(((VelenRatelimitOrdinaryMessage) instance.getVelen().getRatelimitedMessage()).load(
                                        remaining,
                                        user,
                                        event.getChannel(),
                                        instance.getName())
                                );
                            } else {
                                builder.addEmbed(((VelenRatelimitEmbedMessage) instance.getVelen().getRatelimitedMessage()).load(
                                        remaining,
                                        user,
                                        event.getChannel(),
                                        instance.getName())
                                );
                            }

                            builder.send(event.getChannel()).thenAccept(message ->
                                    VelenThreadPool.schedule(() -> {
                                        instance.getVelen().getRatelimiter().release(user.getId(), lock, instance.toString());
                                        message.delete().thenAccept(unused -> event.getMessage().delete());
                                    }, remaining, TimeUnit.SECONDS)
                            ).exceptionally(ExceptionLogger.get());

                        } else {
                            instance.getVelen().getRatelimiter().release(user.getId(), lock, toString());
                            dispatch(event, args);
                        }
                    }, unused -> dispatch(event, args));
        } else {
            dispatch(event, args);
        }

    }

}
