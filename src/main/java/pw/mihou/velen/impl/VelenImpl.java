package pw.mihou.velen.impl;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.util.DiscordRegexPattern;
import org.javacord.api.util.logging.ExceptionLogger;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class VelenImpl implements Velen {

    private final VelenRatelimitMessage ratelimitedMessage;
    private final VelenRatelimiter ratelimiter;
    private final List<VelenCommand> commands;
    private final VelenPrefixManager prefixManager;
    private final VelenPermissionMessage noPermissionMessage;
    private final VelenRoleMessage noRoleMessage;
    private final VelenBlacklist blacklist;
    private final boolean allowMentionPrefix;

    public VelenImpl(VelenRatelimiter ratelimiter, VelenPrefixManager prefixManager, VelenRatelimitMessage ratelimitedMessage,
                     VelenPermissionMessage noPermissionMessage, VelenRoleMessage noRoleMessage,
                     VelenBlacklist blacklist, boolean allowMentionPrefix) {
        this.ratelimiter = ratelimiter;
        this.ratelimitedMessage = ratelimitedMessage;
        this.commands = new ArrayList<>();
        this.prefixManager = prefixManager;
        this.noPermissionMessage = noPermissionMessage;
        this.noRoleMessage = noRoleMessage;
        this.blacklist = blacklist;
        this.allowMentionPrefix = allowMentionPrefix;
    }

    @Override
    public Velen addCommand(VelenCommand command) {
        commands.add(command);
        return this;
    }

    @Override
    public Velen removeCommand(VelenCommand command) {
        commands.remove(command);
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
        return commands;
    }

    @Override
    public List<VelenCommand> getCategory(String category) {
        return commands.stream()
                .filter(velenCommand -> velenCommand.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    @Override
    public List<VelenCommand> getCategoryIgnoreCasing(String category) {
        return commands.stream()
                .filter(velenCommand -> velenCommand.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VelenCommand> getCommand(String command) {
        return commands.stream().filter(velenCommand -> velenCommand.getName().equals(command)).findFirst();
    }

    @Override
    public Optional<VelenCommand> getCommandIgnoreCasing(String command) {
        return commands.stream().filter(velenCommand -> velenCommand.getName().equalsIgnoreCase(command)).findFirst();
    }

    @Override
    public CompletableFuture<Void> registerAllSlashCommands(DiscordApi api) {
        return CompletableFuture.allOf(commands.stream().filter(VelenCommand::supportsSlashCommand)
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
        dispatch(event, event.isServerMessage() && event.getServer().isPresent() ?
                prefixManager.getPrefix(event.getServer().get().getId()) : prefixManager.getDefaultPrefix());
    }

    private boolean startsWithMention(String content, String mentionId) {
        return content.startsWith("<@") && (
                content.startsWith(String.format("<@%s>", mentionId))
                || content.startsWith(String.format("<@!%s>", mentionId))
        );
    }

    private void dispatch(MessageCreateEvent event, String prefix) {
        if (supportsBlacklist() && blacklist.isBlacklisted(event.getMessageAuthor().getId()))
            return;

        String content = event.getMessageContent().trim();

        if (content.startsWith(prefix)) {
            content = content.substring(prefix.length()).trim();
        } else if (allowMentionPrefix && startsWithMention(content, event.getApi().getYourself().getIdAsString())) {
            content = DiscordRegexPattern.USER_MENTION.matcher(content).replaceFirst("").trim();
        } else {
            // is not a command
            return;
        }

        for (VelenCommand command : commands) {
            if (!command.isSlashCommandOnly()) {
                for (String name : command.getShortcuts()) {
                    if (content.startsWith(name + " ")) {
                        String argsStr = content.substring(name.length()).trim();
                        // https://stackoverflow.com/questions/18893390/splitting-on-comma-outside-quotes
                        String[] args = argsStr.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                        ((VelenCommandImpl) command).execute(event, args);
                    }
                }
            }
        }
    }

    private boolean isCommand(String prefix, String arg, String command) {
        return (prefix + command).equalsIgnoreCase(arg);
    }

    private boolean isCommand(String prefix, String message, List<String> commands) {
        return commands.stream().anyMatch(s -> isCommand(prefix, message, s));
    }

    private boolean isMessageOfCommandMention(String[] rawArgs, String command) {
        return rawArgs.length > 1 && rawArgs[1].equalsIgnoreCase(command);
    }

    private boolean isMessageOfAnyCommandMention(String[] rawArgs, List<String> commands) {
        return commands.stream().anyMatch(s -> isMessageOfCommandMention(rawArgs, s));
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        if (supportsBlacklist() && blacklist.isBlacklisted(event.getSlashCommandInteraction()
                .getUser().getId())) return;

        commands.stream().filter(velenCommand -> velenCommand.supportsSlashCommand()
                && velenCommand.getName().toLowerCase().equals(event.getSlashCommandInteraction().getCommandName()))
                .forEachOrdered(velenCommand -> VelenThreadPool.executorService.submit(() -> ((VelenCommandImpl) velenCommand).execute(event)));
    }
}
