package pw.mihou.velen.impl.commands.children;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
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

public class BaseInteractionCommand extends BaseCommandImplementation {

    /**
     * Creates a new Base Interaction Command implementation
     * that can handle the implementation of Slash commands.
     *
     * @param instance The instance to use.
     */
    public BaseInteractionCommand(VelenCommandImpl instance) {
        super(instance);
    }

    /**
     * This handles the incoming command event from Discord
     * and handles the to-do tasks such as authentication and so forth.
     *
     * @param event The message event received.
     */
    public void onReceive(SlashCommandCreateEvent event) {
        User user = event.getSlashCommandInteraction().getUser();

        if (!applyRestraints(event)) {
            return;
        }

        InteractionImmediateResponseBuilder builder = event.getSlashCommandInteraction().createImmediateResponder()
                .setFlags(MessageFlag.EPHEMERAL);
        TextChannel channel = event.getSlashCommandInteraction().getChannel().orElseThrow(() ->
                new IllegalStateException("Text channel not found for slash command, please create an issue @ https://github.com/ShindouMihou/Velen."));

        if (!applySlashRestraints(event)) {
            if (instance.getConditionalMessage() != null) {
                if (instance.getConditionalMessage() instanceof VelenOrdinaryMessage) {
                    builder.setContent(((VelenConditionalOrdinaryMessage) instance.getConditionalMessage())
                            .load(user, channel, instance.getName()));
                } else {
                    builder.addEmbed(((VelenConditionalEmbedMessage) instance.getConditionalMessage())
                            .load(user, channel, instance.getName()));
                }

                builder.respond().exceptionally(ExceptionLogger.get());
            }
            return;
        }

        if (!applyRoleRestraints(event.getInteraction().getServer().orElse(null), user)) {
            builder.setAllowedMentions(VelenUtils.createNoMentions());
            String roles = instance.getRequiredRoles().stream().map(this::toRoleFormat).collect(Collectors.joining(", "));

            if (instance.getVelen().getNoRoleMessage() instanceof VelenOrdinaryMessage) {
                builder.setContent(((VelenRoleOrdinaryMessage) instance.getVelen().getNoRoleMessage())
                        .load(roles, user, channel, instance.getName()));
            } else {
                builder.addEmbed(((VelenRoleEmbedMessage) instance.getVelen().getNoRoleMessage())
                        .load(roles, user, channel, instance.getName()));
            }

            builder.respond().exceptionally(ExceptionLogger.get());
            return;
        }

        if (!applyPermissionRestraint(event.getInteraction().getServer().orElse(null), user)) {
            if (instance.getVelen().getNoPermissionMessage() instanceof VelenOrdinaryMessage) {
                builder.setContent(((VelenPermissionOrdinaryMessage) instance.getVelen().getNoPermissionMessage())
                        .load(instance.getPermissions(), user, channel, instance.getName()));
            } else {
                builder.addEmbed(((VelenPermissionEmbedMessage) instance.getVelen().getNoPermissionMessage())
                        .load(instance.getPermissions(), user, channel, instance.getName()));
            }

            builder.respond().exceptionally(ExceptionLogger.get());
            return;
        }

        if (instance.getCooldown() != null && !(instance.getCooldown().isZero() || instance.getCooldown().isNegative())) {
            long lock = event.getInteraction().getServer().map(Server::getId).orElse(user.getId());
            applyRatelimiter(user.getId(), lock,
                    remaining -> {
                        if (remaining > 0) {
                            if (instance.getVelen().getRatelimitedMessage() instanceof VelenOrdinaryMessage) {
                                builder.setContent(((VelenRatelimitOrdinaryMessage) instance.getVelen().getRatelimitedMessage()).load(
                                        remaining,
                                        user,
                                        channel,
                                        instance.getName())
                                );
                            } else {
                                builder.addEmbed(((VelenRatelimitEmbedMessage) instance.getVelen().getRatelimitedMessage()).load(
                                        remaining,
                                        user,
                                        channel,
                                        instance.getName())
                                );
                            }

                            builder.respond().thenAccept(message ->
                                    VelenThreadPool.schedule(() ->
                                                    instance.getVelen().getRatelimiter().release(user.getId(), lock, instance.toString()),
                                            remaining, TimeUnit.SECONDS)
                            ).exceptionally(ExceptionLogger.get());

                        } else {
                            instance.getVelen().getRatelimiter().release(user.getId(), lock, toString());
                            dispatch(event);
                        }
                    }, unused -> dispatch(event));
        } else {
            dispatch(event);
        }
    }

}
