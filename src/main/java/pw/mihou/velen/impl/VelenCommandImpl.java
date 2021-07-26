package pw.mihou.velen.impl;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.velen.interfaces.*;
import pw.mihou.velen.interfaces.messages.VelenOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.surface.embed.VelenConditionalEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.embed.VelenPermissionEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.embed.VelenRatelimitEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.embed.VelenRoleEmbedMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenConditionalOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenPermissionOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenRatelimitOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.surface.text.VelenRoleOrdinaryMessage;
import pw.mihou.velen.interfaces.messages.types.VelenConditionalMessage;
import pw.mihou.velen.utils.Pair;
import pw.mihou.velen.utils.VelenThreadPool;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VelenCommandImpl implements VelenCommand {

    private final String name;
    private final String usage;
    private final String description;
    private final String category;
    private final Duration cooldown;
    private final List<Long> requiredRoles;
    private final List<Long> requiredUsers;
    private final List<PermissionType> permissions;
    private final boolean serverOnly;
    private final boolean privateOnly;
    private final String[] shortcuts;
    private final VelenEvent velenEvent;
    private final Velen velen;

    private final List<SlashCommandOption> options;
    private final List<Function<MessageCreateEvent, Boolean>> conditions;
    private final List<Function<SlashCommandCreateEvent, Boolean>> conditionsSlash;
    private final VelenConditionalMessage conditionalMessage;
    private final VelenSlashEvent velenSlashEvent;
    private final long serverId;
    private String stringValue;

    public VelenCommandImpl(String name, String usage, String description, String category, Duration cooldown, List<Long> requiredRoles,
                            List<Long> requiredUsers, List<PermissionType> permissions, boolean serverOnly,
                            boolean privateOnly,
                            List<String> shortcuts, VelenEvent event, VelenSlashEvent slashEvent, List<SlashCommandOption> options,
                            List<Function<MessageCreateEvent, Boolean>> conditions,
                            List<Function<SlashCommandCreateEvent, Boolean>> conditionsSlash,
                            VelenConditionalMessage conditionalMessage,
                            long serverId,
                            Velen velen) {
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.category = category;
        this.cooldown = cooldown;
        this.requiredRoles = requiredRoles;
        this.requiredUsers = requiredUsers;
        this.permissions = permissions;
        this.serverOnly = serverOnly;
        // add the name as it can be used for invocation too
        // but don't edit the shortcuts list as it may be immutable which would lead to bugs
        String[] shortcutsArray = shortcuts.toArray(new String[0]);
        String[] shortcutsArrayFinal = Arrays.copyOf(shortcutsArray, shortcutsArray.length + 1);
        shortcutsArrayFinal[shortcutsArrayFinal.length - 1] = name;

        this.shortcuts = shortcutsArrayFinal;
        this.velenEvent = event;
        this.velenSlashEvent = slashEvent;
        this.serverId = serverId;
        this.conditions = conditions;
        this.conditionsSlash = conditionsSlash;
        this.conditionalMessage = conditionalMessage;
        this.privateOnly = privateOnly;
        this.velen = velen;
        this.options = options;
    }

    public void execute(SlashCommandCreateEvent e) {
        SlashCommandInteraction event = e.getSlashCommandInteraction();
        long userId = event.getUser().getId();
        long serverId = event.getServer().map(DiscordEntity::getId).orElse(userId);

        User user = event.getUser();
        if (serverOnly) {
            if (!event.getServer().isPresent())
                return;
        }

        if (privateOnly) {
            if (event.getServer().isPresent())
                return;
        }

        if (!event.getChannel().isPresent())
            return;

        if (!conditionsSlash.isEmpty()) {
            if (!conditionsSlash.stream().allMatch(fun -> fun.apply(e))) {
                if (conditionalMessage != null) {
                    InteractionImmediateResponseBuilder builder =
                            event.createImmediateResponder()
                                    .setFlags(MessageFlag.EPHEMERAL);
                    if (conditionalMessage instanceof VelenOrdinaryMessage)
                        builder.setContent(((VelenConditionalOrdinaryMessage) conditionalMessage).load(user, event.getChannel().get(), name));
                    else
                        builder.addEmbed(((VelenConditionalEmbedMessage) conditionalMessage).load(user, event.getChannel().get(), name));
                    builder.respond().exceptionally(ExceptionLogger.get());
                }
                return;
            }
        }

        if (event.getServer().isPresent()) {
            Server server = event.getServer().get();
            if (!requiredRoles.isEmpty()) {
                Collection<Role> userRoles = user.getRoles(server);
                if (requiredRoles.stream().noneMatch(aLong -> userRoles.stream().anyMatch(role -> role.getId() == aLong))) {
                    InteractionImmediateResponseBuilder builder = event.createImmediateResponder()
                            .setAllowedMentions(new AllowedMentionsBuilder()
                                    .setMentionRoles(false)
                                    .setMentionUsers(false)
                                    .setMentionEveryoneAndHere(false)
                                    .build())
                            .setFlags(MessageFlag.EPHEMERAL);

                    if (velen.getNoRoleMessage() instanceof VelenOrdinaryMessage)
                        builder.setContent(((VelenRoleOrdinaryMessage) velen.getNoRoleMessage())
                                .load(requiredRoles.stream().map(aLong -> "<@&" + aLong + ">")
                                        .collect(Collectors.joining(", ")), user, event.getChannel().get(), name));
                    else
                        builder.addEmbed(((VelenRoleEmbedMessage) velen.getNoRoleMessage())
                                .load(requiredRoles.stream().map(aLong -> "<@&" + aLong + ">")
                                        .collect(Collectors.joining(", ")), user, event.getChannel().get(), name));

                    builder.respond().exceptionally(ExceptionLogger.get());
                    return;
                }
            }

            if (!permissions.isEmpty()) {
                Collection<PermissionType> userPerms = server.getPermissions(user).getAllowedPermission();
                if (!userPerms.containsAll(permissions)) {
                    InteractionImmediateResponseBuilder builder = event.createImmediateResponder()
                            .setFlags(MessageFlag.EPHEMERAL);

                    if (velen.getNoPermissionMessage() instanceof VelenOrdinaryMessage)
                        builder.setContent(((VelenPermissionOrdinaryMessage) velen.getNoPermissionMessage())
                                .load(permissions, user, event.getChannel().get(), name));
                    else
                        builder.addEmbed(((VelenPermissionEmbedMessage) velen.getNoPermissionMessage())
                                .load(permissions, user, event.getChannel().get(), name));

                    builder.respond().exceptionally(ExceptionLogger.get());
                    return;
                }
            }
        }

        if (!requiredUsers.isEmpty()) {
            if (requiredUsers.stream().noneMatch(aLong -> userId == aLong))
                return;
        }

        innerHandle(user, serverId, event);
    }

    public void execute(MessageCreateEvent event, String[] args) {
        long userId = event.getMessageAuthor().getId();
        long serverId = event.isServerMessage() ? event.getServer().get().getId() : event.getMessageAuthor().getId();

        if (!event.getMessageAuthor().isRegularUser() || !event.getMessageAuthor().asUser().isPresent())
            return;

        User user = event.getMessageAuthor().asUser().get();

        if (!conditions.isEmpty()) {
            if (!conditions.stream().allMatch(fun -> fun.apply(event))) {
                if (conditionalMessage != null) {
                    MessageBuilder builder = new MessageBuilder().replyTo(event.getMessage());
                    if (conditionalMessage instanceof VelenOrdinaryMessage)
                        builder.setContent(((VelenConditionalOrdinaryMessage) conditionalMessage).load(user, event.getChannel(), name));
                    else
                        builder.addEmbed(((VelenConditionalEmbedMessage) conditionalMessage).load(user, event.getChannel(), name));
                    builder.send(event.getChannel()).exceptionally(ExceptionLogger.get());
                }
                return;
            }
        }

        if (serverOnly) {
            if (!event.getServer().isPresent())
                return;

            if (serverId != 0L && event.getServer().get().getId() != serverId)
                return;
        }

        if (privateOnly) {
            if (event.getServer().isPresent())
                return;
        }

        if (event.getServer().isPresent()) {
            Server server = event.getServer().get();
            if (!requiredRoles.isEmpty()) {
                Collection<Role> userRoles = user.getRoles(server);
                if (requiredRoles.stream().noneMatch(aLong -> userRoles.stream().anyMatch(role -> role.getId() == aLong))) {
                    MessageBuilder builder = new MessageBuilder()
                            .setAllowedMentions(new AllowedMentionsBuilder()
                                    .setMentionRoles(false)
                                    .setMentionUsers(false)
                                    .setMentionEveryoneAndHere(false)
                                    .build())
                            .replyTo(event.getMessage());

                    if (velen.getNoRoleMessage() instanceof VelenOrdinaryMessage)
                        builder.setContent(((VelenRoleOrdinaryMessage) velen.getNoRoleMessage())
                                .load(requiredRoles.stream().map(aLong -> "<@&" + aLong + ">")
                                        .collect(Collectors.joining(", ")), user, event.getChannel(), name));
                    else
                        builder.addEmbed(((VelenRoleEmbedMessage) velen.getNoRoleMessage())
                                .load(requiredRoles.stream().map(aLong -> "<@&" + aLong + ">")
                                        .collect(Collectors.joining(", ")), user, event.getChannel(), name));

                    builder.send(event.getChannel()).exceptionally(ExceptionLogger.get());
                    return;
                }
            }

            if (!permissions.isEmpty()) {
                Collection<PermissionType> permissionTypes = server.getPermissions(user).getAllowedPermission();
                if (!permissionTypes.containsAll(permissions)) {
                    MessageBuilder builder = new MessageBuilder().replyTo(event.getMessage());

                    if (velen.getNoPermissionMessage() instanceof VelenOrdinaryMessage)
                        builder.setContent(((VelenPermissionOrdinaryMessage) velen.getNoPermissionMessage())
                                .load(permissions, user, event.getChannel(), name));
                    else
                        builder.addEmbed(((VelenPermissionEmbedMessage) velen.getNoPermissionMessage())
                                .load(permissions, user, event.getChannel(), name));

                    builder.send(event.getChannel()).exceptionally(ExceptionLogger.get());
                    return;
                }
            }
        }

        if (!requiredUsers.isEmpty()) {
            if (requiredUsers.stream().noneMatch(aLong -> userId == aLong))
                return;
        }


        innerHandle(user, serverId, event, args);
    }

    private void innerHandle(User user, long server, MessageCreateEvent event, String[] args) {
        if (cooldown != null && !(cooldown.isZero() || cooldown.isNegative())) {
            velen.getRatelimiter().ratelimit(user.getId(), server, toString(), cooldown.toMillis(), remaining -> {
                if (remaining > 0) {
                    MessageBuilder builder = new MessageBuilder().replyTo(event.getMessage());

                    if (velen.getRatelimitedMessage() instanceof VelenOrdinaryMessage) {
                        builder.setContent(((VelenRatelimitOrdinaryMessage) velen.getRatelimitedMessage())
                                .load(remaining,
                                        user,
                                        event.getChannel(),
                                        name));
                    } else {
                        builder.addEmbed(((VelenRatelimitEmbedMessage) velen.getRatelimitedMessage())
                                .load(remaining,
                                        user,
                                        event.getChannel(),
                                        name));
                    }

                    builder.send(event.getChannel()).thenAccept(message ->
                            VelenThreadPool.schedule(() -> {
                                velen.getRatelimiter().release(user.getId(), server, toString());
                                message.delete().thenAccept(unused -> event.getMessage().delete());
                            }, remaining, TimeUnit.SECONDS)).exceptionally(ExceptionLogger.get());

                } else {
                    velen.getRatelimiter().release(user.getId(), server, toString());
                }
            }, ratelimitEntity -> runEvent(event, args));
        } else {
            runEvent(event, args);
        }
    }

    private void innerHandle(User user, long server, SlashCommandInteraction event) {
        if (!event.getChannel().isPresent())
            return;

        if (cooldown != null && !(cooldown.isZero() || cooldown.isNegative())) {
            velen.getRatelimiter().ratelimit(user.getId(), server, toString(), cooldown.toMillis(), remaining -> {
                if (remaining > 0) {
                    InteractionImmediateResponseBuilder builder = event.createImmediateResponder()
                            .setFlags(MessageFlag.EPHEMERAL);

                    if (velen.getRatelimitedMessage() instanceof VelenOrdinaryMessage) {
                        builder.setContent(((VelenRatelimitOrdinaryMessage) velen.getRatelimitedMessage())
                                .load(remaining, user, event.getChannel().get(), name));
                    } else {
                        builder.addEmbed(((VelenRatelimitEmbedMessage) velen.getRatelimitedMessage())
                                .load(remaining, user, event.getChannel().get(), name));
                    }

                    builder.respond().thenAccept(eg -> VelenThreadPool.schedule(() -> {
                        velen.getRatelimiter().release(user.getId(), server, toString());
                        eg.delete();
                    }, remaining, TimeUnit.SECONDS)).exceptionally(ExceptionLogger.get());

                } else {
                    velen.getRatelimiter().release(user.getId(), server, toString());
                }
            }, ratelimitEntity -> runEvent(event));
        } else {
            runEvent(event);
        }
    }

    private void runEvent(MessageCreateEvent event, String[] args) {
        event.getMessageAuthor().asUser().ifPresent(user -> {
            if (velenEvent instanceof VelenServerEvent) {
                event.getServer().ifPresent(server -> ((VelenServerEvent) velenEvent).onEvent(event, event.getMessage(),
                        server, user, args));
            } else {
                velenEvent.onEvent(event, event.getMessage(), user, args);
            }
        });
    }

    public Pair<Long, SlashCommandBuilder> asSlashCommand() {
        SlashCommandBuilder builder = SlashCommand.with(name.toLowerCase(), description);

        if (options != null)
            return Pair.of(serverId, builder.setOptions(options));

        return Pair.of(serverId, builder);
    }

    @Override
    public boolean isSlashCommandOnly() {
        return velenEvent == null;
    }

    private void runEvent(SlashCommandInteraction event) {
        if (velenSlashEvent == null)
            throw new RuntimeException("A slash command event was received for " + getName() + " but there is no event" +
                    " handler for the slash command found!");

        velenSlashEvent.onEvent(event, event.getUser(),
                new VelenSlashCommandArguments(event.getOptions()), event.getOptions(),
                event.createImmediateResponder());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Duration getCooldown() {
        return cooldown;
    }

    public String getUsage() {
        return usage;
    }

    @Override
    public List<Long> getRequiredRoles() {
        return requiredRoles;
    }

    @Override
    public List<Long> getRequiredUsers() {
        return requiredUsers;
    }

    @Override
    public List<PermissionType> getPermissions() {
        return permissions;
    }

    @Override
    public boolean isServerOnly() {
        return serverOnly;
    }

    @Override
    public boolean supportsSlashCommand() {
        return velenSlashEvent != null;
    }

    @Override
    public String[] getShortcuts() {
        return shortcuts;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VelenCommandImpl that = (VelenCommandImpl) o;
        return isServerOnly() == that.isServerOnly() &&
                getName().equals(that.getName()) &&
                Objects.equals(getUsage(), that.getUsage()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getCooldown(), that.getCooldown()) &&
                Objects.equals(getRequiredRoles(), that.getRequiredRoles()) &&
                Objects.equals(getRequiredUsers(), that.getRequiredUsers()) &&
                Objects.equals(getPermissions(), that.getPermissions()) &&
                Objects.equals(getShortcuts(), that.getShortcuts()) &&
                velenEvent.equals(that.velenEvent) &&
                velen.equals(that.velen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getUsage(), getDescription(), getCooldown(), getRequiredRoles(), getRequiredUsers(), getPermissions(), isServerOnly(), getShortcuts(), velenEvent, velen);
    }

    @Override
    public String toString() {
        if (stringValue != null && !stringValue.isEmpty())
            return stringValue;

        stringValue = name + " (Description: " + description + ", Cooldown: " + cooldown.toMillis() + ", Slash: " + supportsSlashCommand()
                + ", Hybrid: " + (!isSlashCommandOnly() && supportsSlashCommand()) + ")";
        return stringValue;
    }
}
