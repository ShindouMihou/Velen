package pw.mihou.velen.impl;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.VelenEvent;
import pw.mihou.velen.interfaces.VelenServerEvent;
import pw.mihou.velen.utils.Scheduler;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
    private final List<String> shortcuts;
    private final VelenEvent velenEvent;
    private final Velen velen;

    public VelenCommandImpl(String name, String usage, String description, String category, Duration cooldown, List<Long> requiredRoles,
                            List<Long> requiredUsers, List<PermissionType> permissions, boolean serverOnly,
                            List<String> shortcuts, VelenEvent event, Velen velen) {
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.category = category;
        this.cooldown = cooldown;
        this.requiredRoles = requiredRoles;
        this.requiredUsers = requiredUsers;
        this.permissions = permissions;
        this.serverOnly = serverOnly;
        this.shortcuts = shortcuts;
        this.velenEvent = event;
        this.velen = velen;
    }

    public void execute(MessageCreateEvent event, String[] args) {
        long userId = event.getMessageAuthor().getId();
        long serverId = event.isServerMessage() ? event.getServer().get().getId() : event.getMessageAuthor().getId();

        if (!event.getMessageAuthor().isRegularUser() || !event.getMessageAuthor().asUser().isPresent())
            return;

        User user = event.getMessageAuthor().asUser().get();
        if (serverOnly) {
            if (!event.getServer().isPresent())
                return;
        }

        if (event.getServer().isPresent()) {
            Server server = event.getServer().get();
            if (!requiredRoles.isEmpty()) {
                if (requiredRoles.stream().noneMatch(aLong -> user.getRoles(server).stream().anyMatch(role -> role.getId() == aLong))) {
                    new MessageBuilder().setAllowedMentions(new AllowedMentionsBuilder().setMentionRoles(false)
                            .setMentionUsers(false).setMentionEveryoneAndHere(false).build()).setContent(velen.getNoRoleMessage()
                            .load(requiredRoles.stream().map(aLong -> "<@&" + aLong + ">")
                                    .collect(Collectors.joining(", ")), user, event.getChannel(), name))
                            .replyTo(event.getMessage())
                            .send(event.getChannel())
                            .exceptionally(ExceptionLogger.get());
                    return;
                }
            }

            if (!permissions.isEmpty()) {
                if (permissions.stream().allMatch(permissionType -> server.getPermissions(user)
                        .getAllowedPermission().stream().anyMatch(type -> type.getValue() == permissionType.getValue()))) {
                    event.getMessage().reply(velen.getNoPermissionMessage()
                            .load(permissions, user, event.getChannel(), name))
                            .exceptionally(ExceptionLogger.get());
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
                    event.getMessage().reply(velen.getRatelimitedMessage()
                            .load(remaining,
                                    user,
                                    event.getChannel(),
                                    name)).thenAccept(message ->
                            Scheduler.schedule(() -> {
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
    public List<String> getShortcuts() {
        return shortcuts;
    }

    @Override
    public String getCategory(){
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
        return name + "(Description: " + description + ")";
    }
}
