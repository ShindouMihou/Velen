package pw.mihou.velen.impl;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandUpdater;
import org.javacord.api.util.logging.ExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.interfaces.messages.types.VelenPermissionMessage;
import pw.mihou.velen.interfaces.messages.types.VelenRatelimitMessage;
import pw.mihou.velen.interfaces.messages.types.VelenRoleMessage;
import pw.mihou.velen.internals.VelenBlacklist;
import pw.mihou.velen.prefix.VelenPrefixManager;
import pw.mihou.velen.ratelimiter.VelenRatelimiter;
import pw.mihou.velen.utils.Pair;
import pw.mihou.velen.utils.VelenThreadPool;
import pw.mihou.velen.utils.VelenUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class VelenImpl implements Velen {

    private final VelenRatelimitMessage ratelimitedMessage;
    private final VelenRatelimiter ratelimiter;
    // We want to use O(1) for full commands and the normal way for shortcuts.
    private final HashMap<String, VelenCommand> commands;
    private final VelenPrefixManager prefixManager;
    private final VelenPermissionMessage noPermissionMessage;
    private final VelenRoleMessage noRoleMessage;
    private final VelenBlacklist blacklist;
    private final boolean allowMentionPrefix;
    private static final Logger commandInterceptorLogger = LoggerFactory.getLogger("Velen - Command Interceptor");

    public VelenImpl(VelenRatelimiter ratelimiter, VelenPrefixManager prefixManager, VelenRatelimitMessage ratelimitedMessage,
                     VelenPermissionMessage noPermissionMessage, VelenRoleMessage noRoleMessage,
                     VelenBlacklist blacklist, boolean allowMentionPrefix) {
        this.ratelimiter = ratelimiter;
        this.ratelimitedMessage = ratelimitedMessage;
        this.commands = new HashMap<>();
        this.prefixManager = prefixManager;
        this.noPermissionMessage = noPermissionMessage;
        this.noRoleMessage = noRoleMessage;
        this.blacklist = blacklist;
        this.allowMentionPrefix = allowMentionPrefix;
    }

    @Override
    public Velen addCommand(VelenCommand command) {
        commands.put(command.getName().toLowerCase(), command);
        return this;
    }

    @Override
    public Velen removeCommand(VelenCommand command) {
        commands.remove(command.getName().toLowerCase());
        return this;
    }

    @Override
    public synchronized Velen getInstance() {
        return this;
    }

    @Override
    public VelenRatelimitMessage getRatelimitedMessage() {
        return ratelimitedMessage;
    }

    @Override
    public VelenPermissionMessage getNoPermissionMessage() {
        return noPermissionMessage;
    }

    @Override
    public VelenRoleMessage getNoRoleMessage() {
        return noRoleMessage;
    }

    @Override
    public VelenRatelimiter getRatelimiter() {
        return ratelimiter;
    }

    @Override
    public List<VelenCommand> getCommands() {
        return new ArrayList<>(commands.values());
    }

    @Override
    public List<VelenCommand> getCategory(String category) {
        return commands.values().stream()
                .filter(velenCommand -> velenCommand.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    @Override
    public List<VelenCommand> getCategoryIgnoreCasing(String category) {
        return commands.values().stream()
                .filter(velenCommand -> velenCommand.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<VelenCommand>> getCategories() {
        Map<String, List<VelenCommand>> catMap = new HashMap<>();
        commands.values()
                .forEach(velenCommand -> {
                    if(velenCommand.getCategory().isEmpty())
                        return;

                    if(!catMap.containsKey(velenCommand.getCategory()))
                        catMap.put(velenCommand.getCategory(), new ArrayList<>());

                    catMap.get(velenCommand.getCategory()).add(velenCommand);
                });

        // We want the list to be returned as an immutable list.
        return catMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> Collections.unmodifiableList(e.getValue())));
    }

    @Override
    public Optional<VelenCommand> getCommand(String command) {
        return Optional.ofNullable(commands.get(command.toLowerCase()));
    }

    @Override
    @Deprecated
    public Optional<VelenCommand> getCommandIgnoreCasing(String command) {
        return getCommand(command);
    }

    @Override
    public CompletableFuture<Void> registerAllSlashCommands(DiscordApi api) {
        return CompletableFuture.allOf(commands.values().stream().filter(VelenCommand::supportsSlashCommand)
                .map(velenCommand -> {
                    Pair<Long, SlashCommandBuilder> pair = ((VelenCommandImpl) velenCommand).asSlashCommand();

                    if (pair.getLeft() != null && pair.getLeft() != 0L) {
                        Optional<Server> server = api.getServerById(pair.getLeft());
                        if (server.isPresent()) {
                            return pair.getRight().createForServer(server.get());
                        } else {
                            throw new IllegalArgumentException("Server " + pair.getLeft() + " couldn't be found for " +
                                    "slash command: " + pair.getRight().toString());
                        }
                    }

                    return pair.getRight().createGlobal(api);
                }).toArray(CompletableFuture[]::new)).exceptionally(ExceptionLogger.get());
    }

    @Override
    public CompletableFuture<SlashCommand> registerSlashCommand(String command, DiscordApi api) {
        if(!commands.containsKey(command.toLowerCase()))
            throw new IllegalArgumentException("The command " + command + " couldn't be found!");

        VelenCommand c = commands.get(command.toLowerCase());
        if(!c.supportsSlashCommand())
            throw new IllegalArgumentException("The command " + command + " does not support slash commands!");

        Pair<Long, SlashCommandBuilder> pair = ((VelenCommandImpl) c).asSlashCommand();
        if (pair.getLeft() != null && pair.getLeft() != 0L) {
            Optional<Server> server = api.getServerById(pair.getLeft());
            if (server.isPresent()) {
                return pair.getRight().createForServer(server.get());
            } else {
                throw new IllegalArgumentException("Server " + pair.getLeft() + " couldn't be found for " +
                        "slash command: " + pair.getRight().toString());
            }
        }

        return pair.getRight().createGlobal(api);
    }

    @Override
    public CompletableFuture<SlashCommand> updateSlashCommand(long id, String command, DiscordApi api) {
        if(!commands.containsKey(command.toLowerCase()))
            throw new IllegalArgumentException("The command " + command + " couldn't be found!");

        VelenCommand c = commands.get(command.toLowerCase());
        if(!c.supportsSlashCommand())
            throw new IllegalArgumentException("The command " + command + " does not support slash commands!");

        return updateSlashCommand(id, c, api);
    }

    @Override
    public CompletableFuture<SlashCommand> updateSlashCommand(long id, VelenCommand command, DiscordApi api) {
        Pair<Long, SlashCommandUpdater> pair = ((VelenCommandImpl) command).asSlashCommandUpdater(id);
        if (pair.getLeft() != null && pair.getLeft() != 0L) {
            Optional<Server> server = api.getServerById(pair.getLeft());
            if (server.isPresent()) {
                return pair.getRight().updateForServer(server.get());
            } else {
                throw new IllegalArgumentException("Server " + pair.getLeft() + " couldn't be found for " +
                        "slash command: " + pair.getRight().toString());
            }
        }

        return pair.getRight().updateGlobal(api);
    }

    @Override
    public CompletableFuture<Map<Long, String>> getAllSlashCommandIds(DiscordApi api) {
        return api.getGlobalSlashCommands().thenApply(slashCommands -> slashCommands.stream()
                .collect(Collectors.toMap(SlashCommand::getId, SlashCommand::getName)));
    }

    @Override
    public boolean supportsBlacklist() {
        return blacklist != null;
    }

    @Override
    public VelenBlacklist getBlacklist() {
        return blacklist;
    }

    @Override
    public VelenPrefixManager getPrefixManager() {
        return prefixManager;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        dispatch(event, event.getMessageContent().split("\\s+"), event.isServerMessage() && event.getServer().isPresent() ?
                prefixManager.getPrefix(event.getServer().get().getId()) : prefixManager.getDefaultPrefix());
    }

    private void dispatch(MessageCreateEvent event, String[] args, String prefix) {
        if(!event.getMessageAuthor().isRegularUser())
            return;

        if (supportsBlacklist() && blacklist.isBlacklisted(event.getMessageAuthor().getId()))
            return;

        boolean isUsingMention = allowMentionPrefix && VelenUtils
                .startsWithMention(event.getMessageContent(), event.getApi().getYourself().getIdAsString());

        // This exists to prevent an issue where cmd returns over index exception.
        if(isUsingMention) {
            if(args.length < 2)
                return;
        } else {
            if(!args[0].startsWith(prefix))
                return;
        }

        // kArgs will erase the mention if
        String kArgs = event.getMessageContent();

        String cmd = isUsingMention ? args[1] : args[0].substring(prefix.length());
        if(commands.containsKey(cmd)) {
            VelenCommand command = commands.get(cmd);
            if(!command.isSlashCommandOnly()) {
                commandInterceptorLogger.debug("Intercepted trigger for command ({}) with packet (message={}, args={}, user={}).",
                        command.getName(), event.getMessageContent(),
                        Arrays.toString(VelenUtils.splitContent(kArgs)),
                        event.getMessageAuthor().getId());

                VelenThreadPool.executorService.submit(() -> ((VelenCommandImpl) command).execute(event,
                        VelenUtils.splitContent(kArgs)));
            }
        } else {
            commands.values().stream()
                    .filter(velenCommand -> !velenCommand.isSlashCommandOnly())
                    .filter(velenCommand -> Arrays.stream(velenCommand.getShortcuts()).anyMatch(cmd::equalsIgnoreCase))
                    .forEachOrdered(command -> {
                        commandInterceptorLogger.debug("Intercepted trigger for command ({}) with packet (message={}, args={}, user={}).",
                                command.getName(), event.getMessageContent(),
                                Arrays.toString(VelenUtils.splitContent(kArgs)),
                                event.getMessageAuthor().getId());

                        VelenThreadPool.executorService.submit(() -> ((VelenCommandImpl) command).execute(event,
                                VelenUtils.splitContent(kArgs)));
                    });
        }
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if (supportsBlacklist() && blacklist.isBlacklisted(event.getSlashCommandInteraction()
                .getUser().getId())) return;

        if(commands.containsKey(event.getSlashCommandInteraction().getCommandName())
                && commands.get(event.getSlashCommandInteraction().getCommandName().toLowerCase()).supportsSlashCommand()) {
            // Log it!
            commandInterceptorLogger.debug("Intercepted trigger for command ({}) with packet (type=interaction, user={}).",
                    commands.get(event.getSlashCommandInteraction().getCommandName().toLowerCase()).getName(),
                    event.getInteraction().getUser().getId());

            VelenThreadPool.executorService.submit(() -> ((VelenCommandImpl) commands.get(event.getSlashCommandInteraction()
                    .getCommandName().toLowerCase())).execute(event));
        }
    }
}
