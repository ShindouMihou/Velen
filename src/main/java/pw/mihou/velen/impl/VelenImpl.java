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
import pw.mihou.velen.interfaces.*;
import pw.mihou.velen.interfaces.messages.types.VelenPermissionMessage;
import pw.mihou.velen.interfaces.messages.types.VelenRatelimitMessage;
import pw.mihou.velen.interfaces.messages.types.VelenRoleMessage;
import pw.mihou.velen.interfaces.middleware.VelenMiddleware;
import pw.mihou.velen.internals.VelenBlacklist;
import pw.mihou.velen.internals.mirror.VelenCategorizer;
import pw.mihou.velen.internals.mirror.VelenMirror;
import pw.mihou.velen.prefix.VelenPrefixManager;
import pw.mihou.velen.ratelimiter.VelenRatelimiter;
import pw.mihou.velen.utils.Pair;
import pw.mihou.velen.utils.VelenThreadPool;
import pw.mihou.velen.utils.VelenUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class VelenImpl implements Velen {

    private final VelenRatelimitMessage ratelimitedMessage;
    private final VelenRatelimiter ratelimiter;
    // We want to use O(1) for full commands and the normal way for shortcuts.
    private final HashMap<String, VelenCommand> commands;
    private final HashMap<String, VelenCategory> categories;
    private final VelenPrefixManager prefixManager;
    private final VelenPermissionMessage noPermissionMessage;
    private final VelenRoleMessage noRoleMessage;
    private final VelenBlacklist blacklist;
    private final Map<String, VelenMiddleware> middlewares;
    private final boolean allowMentionPrefix;
    private static final Logger commandInterceptorLogger = LoggerFactory.getLogger("Velen - Command Interceptor");
    private final HandlerStorage handlerStorage = new HandlerStorage();
    private final VelenMirror mirror = new VelenMirror(this);
    private final VelenCategorizer categorizer = new VelenCategorizer(this);

    public VelenImpl(VelenRatelimiter ratelimiter, VelenPrefixManager prefixManager, VelenRatelimitMessage ratelimitedMessage,
                     VelenPermissionMessage noPermissionMessage, VelenRoleMessage noRoleMessage,
                     VelenBlacklist blacklist, boolean allowMentionPrefix) {
        this.ratelimiter = ratelimiter;
        this.ratelimitedMessage = ratelimitedMessage;
        this.commands = new HashMap<>();
        this.middlewares = new HashMap<>();
        this.categories = new HashMap<>();
        this.prefixManager = prefixManager;
        this.noPermissionMessage = noPermissionMessage;
        this.noRoleMessage = noRoleMessage;
        this.blacklist = blacklist;
        this.allowMentionPrefix = allowMentionPrefix;
    }


    /**
     * A private method that should only be internally accessed that
     * allows access to the handler storage of Velen.
     *
     * @return The storage for handlers.
     */
    public HandlerStorage getHandlerStorage() {
        return handlerStorage;
    }

    @Override
    public Velen addCommand(VelenCommand command) {
        commands.put(command.getName().toLowerCase(), command);
        return this;
    }

    @Override
    public Velen loadFrom(String directory) {
        loadFrom(new File(directory));
        return this;
    }

    @Override
    public Velen loadFrom(File directory) {
        if (directory.isDirectory()) {
            load(Objects.requireNonNull(directory.listFiles((dir, name) -> dir.isDirectory() || name.endsWith(".velen"))));
            return this;
        }

        load(directory);
        return this;
    }

    @Override
    public Velen load(File... files) {

        for (File file : files) {
            System.out.println(file.getName());
            iterateAndLoad(file);
        }

        return this;
    }

    @Override
    public Optional<VelenMiddleware> getMiddleware(String name) {
        return Optional.ofNullable(middlewares.get(name.toLowerCase()));
    }

    @Override
    public Velen storeMiddleware(String name, VelenMiddleware middleware) {
        middlewares.put(name.toLowerCase(), middleware);
        return this;
    }

    private void iterateAndLoad(File file) {
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles((dir, name) -> dir.isDirectory() || name.endsWith(".velen") || name.endsWith(".vecomp")))) {
                iterateAndLoad(f);
            }
        } else if (file.getName().endsWith(".velen")){
            mirror.comprehend(file);
        } else if (file.getName().endsWith(".vecomp")) {
            categorizer.comprehend(file);
        }
    }

    @Override
    public Velen addHandler(String name, VelenEvent handler) {
        handlerStorage.addHandler(name, handler);
        return this;
    }

    @Override
    public Velen addHandler(String name, VelenSlashEvent handler) {
        handlerStorage.addHandler(name, handler);
        return this;
    }

    @Override
    public Velen addHandler(String name, VelenHybridHandler handler) {
        handlerStorage.addHandler(name, handler);
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
    public VelenCategory findCategory(String category) {
        return categories.get(category.toLowerCase());
    }

    @Override
    public Map<String, VelenCategory> findCategories() {
        return categories;
    }

    @Override
    public Velen addCategory(VelenCategory category) {
        categories.put(category.getName().toLowerCase(), category);
        return this;
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
                .stream()
                .filter(command -> !command.getCategory().isEmpty())
                .forEach(velenCommand -> {
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
                    Pair<Long, SlashCommandBuilder> pair = velenCommand.asSlashCommand();

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

        Pair<Long, SlashCommandBuilder> pair = c.asSlashCommand();
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
        Pair<Long, SlashCommandUpdater> pair = command.asSlashCommandUpdater(id);
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

                VelenThreadPool.executorService.submit(() -> ((VelenCommandImpl) command).onReceive(event,
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

                        VelenThreadPool.executorService.submit(() -> ((VelenCommandImpl) command).onReceive(event,
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
                    .getCommandName().toLowerCase())).onReceive(event));
        }
    }

    public class HandlerStorage {
        private final Map<String, VelenEvent> messageHandlers = new HashMap<>();
        private final Map<String, VelenHybridHandler> hybridHandlers = new HashMap<>();
        private final Map<String, VelenSlashEvent> slashHandlers = new HashMap<>();

        /**
         * Finds a handler for a message command.
         *
         * @param name The name to search for.
         * @return The handler for message events.
         */
        public Optional<VelenEvent> findMessageHandler(String name) {
            return Optional.ofNullable(messageHandlers.get(name));
        }

        /**
         * Finds a handler for a both command.
         *
         * @param name The name to search for.
         * @return The handler for both events.
         */
        public Optional<VelenHybridHandler> findHybridHandlers(String name) {
            return Optional.ofNullable(hybridHandlers.get(name));
        }

        /**
         * Finds a handler for a slash command.
         *
         * @param name The name to search for.
         * @return The handler for slash events.
         */
        public Optional<VelenSlashEvent> findSlashHandlers(String name) {
            return Optional.ofNullable(slashHandlers.get(name));
        }

        /**
         * Checks if a handler with the name exists in any of the three.
         *
         * @param name The name to search for.
         * @return Does it exists.
         */
        public boolean contains(String name) {
            return messageHandlers.containsKey(name) || slashHandlers.containsKey(name) || hybridHandlers.containsKey(name);
        }

        /**
         * Adds a new handler, it must be of either {@link VelenSlashEvent}, {@link VelenEvent} or {@link VelenHybridHandler}.
         * @param name The name of the handler, it must be unique otherwise it would collide.
         * @param object The object to place.
         */
        public <T> void addHandler(String name, T object) {
            if (object instanceof VelenSlashEvent) {
                slashHandlers.put(name, (VelenSlashEvent) object);
                return;
            }

            if (object instanceof VelenHybridHandler) {
                hybridHandlers.put(name, (VelenHybridHandler) object);
                return;
            }

            if (object instanceof VelenEvent) {
                messageHandlers.put(name, (VelenEvent) object);
                return;
            }

            throw new IllegalArgumentException("Adding of handler with name ["+name+"] failed with error: " +
                    "The handler does not extend any of the handler types (VelenSlashEvent, VelenHybridHandler and VelenEvent).");
        }
    }
}
