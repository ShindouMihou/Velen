package pw.mihou.velen.impl;

import org.javacord.api.event.message.MessageCreateEvent;
import pw.mihou.velen.builders.VelenMessage;
import pw.mihou.velen.builders.VelenPermissionMessage;
import pw.mihou.velen.builders.VelenRoleMessage;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.prefix.VelenPrefixManager;
import pw.mihou.velen.ratelimiter.VelenRatelimiter;
import pw.mihou.velen.utils.Scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VelenImpl implements Velen {

    private final VelenMessage ratelimitedMessage;
    private final VelenRatelimiter ratelimiter;
    private final List<VelenCommand> commands;
    private final VelenPrefixManager prefixManager;
    private final VelenPermissionMessage noPermissionMessage;
    private final VelenRoleMessage noRoleMessage;

    public VelenImpl(VelenRatelimiter ratelimiter, VelenPrefixManager prefixManager, VelenMessage ratelimitedMessage,
                     VelenPermissionMessage noPermissionMessage, VelenRoleMessage noRoleMessage) {
        this.ratelimiter = ratelimiter;
        this.ratelimitedMessage = ratelimitedMessage;
        this.commands = new ArrayList<>();
        this.prefixManager = prefixManager;
        this.noPermissionMessage = noPermissionMessage;
        this.noRoleMessage = noRoleMessage;
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
    public VelenMessage getRatelimitedMessage() {
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
    public VelenPrefixManager getPrefixManager() {
        return prefixManager;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        dispatch(event, event.getMessageContent().split("\\s+"), event.isServerMessage() && event.getServer().isPresent() ?
                prefixManager.getPrefix(event.getServer().get().getId()) : prefixManager.getDefaultPrefix());
    }

    private void dispatch(MessageCreateEvent event, String[] args, String prefix) {
        commands.stream().filter(command -> isCommand(prefix, args[0], command.getName())
                || isCommand(prefix, args[0], command.getShortcuts()))
                .forEachOrdered(command -> Scheduler.executorService
                        .submit(() -> ((VelenCommandImpl) command)
                                .execute(event, args.length > 1 ? Arrays.copyOfRange(args, 1, args.length)
                                        : new String[]{})));
    }

    private boolean isCommand(String prefix, String arg, String command) {
        return (prefix + command).equalsIgnoreCase(arg);
    }

    private boolean isCommand(String command, String data) {
        return command.equalsIgnoreCase(data);
    }

    private boolean isCommand(String prefix, String message, List<String> commands) {
        return commands.stream().anyMatch(s -> isCommand(prefix, message, s));
    }

    private boolean isCommand(String message, List<String> commands) {
        return commands.stream().anyMatch(s -> isCommand(message, s));
    }
}
