package pw.mihou.velen.impl;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.mihou.velen.interfaces.*;
import pw.mihou.velen.interfaces.afterware.VelenAfterware;
import pw.mihou.velen.interfaces.extensions.VelenCompany;
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
    private final Company company = new Company(this);
    private final HashMap<String, VelenCategory> categories;
    private final VelenPrefixManager prefixManager;
    private final VelenPermissionMessage noPermissionMessage;
    private final VelenRoleMessage noRoleMessage;
    private final Warehouse warehouse = new Warehouse();
    private final VelenBlacklist blacklist;
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
        return company.addCommand(command);
    }

    @Override
    public CompletableFuture<Void> index(DiscordApi api) {
        return company.index(api);
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
            iterateAndLoad(file);
        }

        return this;
    }

    @Override
    public Optional<VelenAfterware> getAfterware(String name) {
        return Optional.ofNullable(warehouse.getAfterware(name));
    }

    @Override
    public Velen storeAfterware(String name, VelenAfterware afterware) {
        warehouse.addAfterware(name, afterware);
        return this;
    }

    @Override
    public Optional<VelenMiddleware> getMiddleware(String name) {
        return Optional.ofNullable(warehouse.getMiddleware(name));
    }

    @Override
    public Velen storeMiddleware(String name, VelenMiddleware middleware) {
        warehouse.addMiddleware(name, middleware);
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
        return company.getCommands();
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
    public Optional<VelenCommand> getCommand(String command) {
        return company.getCommand(command);
    }

    @Override
    public Optional<VelenCommand> getCommand(long id) {
        return company.getCommand(id);
    }

    @Override
    public Optional<VelenCommand> getCommand(String command, long server) {
        return company.getCommand(command, server);
    }

    @Override
    public Velen removeCommand(VelenCommand command) {
        return company.removeCommand(command);
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

        Optional<VelenCommand> optional = getCommand(cmd);
        if(optional.isPresent()) {
            VelenCommand command = optional.get();
            if(!command.isSlashCommandOnly()) {
                commandInterceptorLogger.debug("Intercepted trigger for command ({}) with packet (message={}, args={}, user={}).",
                        command.getName(), event.getMessageContent(),
                        Arrays.toString(VelenUtils.splitContent(kArgs)),
                        event.getMessageAuthor().getId());

                VelenThreadPool.executorService.submit(() -> ((VelenCommandImpl) command).onReceive(event,
                        VelenUtils.splitContent(kArgs)));
            }
        } else {
            getCommands().stream()
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

        // We'll search by index first.
        Optional<VelenCommand> indexed = getCommand(event.getSlashCommandInteraction().getCommandId());

        if (indexed.isPresent()) {
            VelenCommand command = indexed.get();

            commandInterceptorLogger.debug("Intercepted trigger for command ({}) with packet (type=interaction, user={}).",
                    command.getName(), event.getInteraction().getUser().getId());

            VelenThreadPool.executorService.submit(() -> ((VelenCommandImpl) command).onReceive(event));
        } else {

            List<VelenCommand> commands;
            if (company.hasIndex(event.getSlashCommandInteraction().getCommandName().toLowerCase()) && event.getInteraction().getServer().isPresent()) {
                // If a global command does have an index and it isn't this command, then that means
                // we are dealing with a server-specific slash command.
                commands = getCommands()
                        .stream()
                        .filter(VelenCommand::supportsSlashCommand)
                        .filter(cmd -> cmd.getName().equalsIgnoreCase(event.getSlashCommandInteraction().getCommandName()))
                        .filter(cmd -> cmd.isServerOnly() && cmd.getServerId() == event.getInteraction().getServer().orElseThrow(AssertionError::new).getId())
                        .collect(Collectors.toList());
            } else {
                // If there is no index for any global commands with the same name.
                // then that means there is just no indexes, then we can just continue on with O(N) lookup.
                commands = getCommands()
                        .stream()
                        .filter(VelenCommand::supportsSlashCommand)
                        .filter(cmd -> cmd.getName().equalsIgnoreCase(event.getSlashCommandInteraction().getCommandName()))
                        .collect(Collectors.toList());
            }

            VelenThreadPool.executorService.submit(() -> commands.forEach(command -> {
                commandInterceptorLogger.debug("Intercepted trigger for command ({}) with packet (type=interaction, user={}).", command.getName(),
                        event.getInteraction().getUser().getId());
                ((VelenCommandImpl) command).onReceive(event);
            }));
        }
    }

    public static class Company implements VelenCompany {

        public final List<VelenCommand> commands = new ArrayList<>();
        public final Map<Long, VelenCommand> indexes = new HashMap<>();

        private Map<String, Long> indexMap;
        private final Velen velen;
        private static final Logger logger = LoggerFactory.getLogger("Velen - Company");

        public Company(Velen velen) {
            this.velen = velen;
        }

        @Override
        public List<VelenCommand> getCommands() {
            return commands;
        }

        @Override
        public Optional<VelenCommand> getCommand(String command) {
            return commands.stream().filter(velenCommand -> velenCommand.getName().equalsIgnoreCase(command))
                    .findFirst();
        }

        /**
         * Checks if a command has an index.
         *
         * @param command The command to lookup.
         * @return Does this command have an index?
         */
        public boolean hasIndex(String command) {
            return indexMap.containsKey(command);
        }

        @Override
        public Optional<VelenCommand> getCommand(long id) {
            return indexes.containsKey(id) ? Optional.of(indexes.get(id)) : Optional.empty();
        }

        @Override
        public Optional<VelenCommand> getCommand(String command, long server) {
            return commands.stream()
                    .filter(cmd -> cmd.getName().equalsIgnoreCase(command) && cmd.isServerOnly() &&cmd.getServerId() == server)
                    .findFirst();
        }

        @Override
        public Velen removeCommand(VelenCommand command) {
            commands.remove(command);
            return velen;
        }

        @Override
        public Velen addCommand(VelenCommand command) {
            commands.add(command);

            if (indexMap == null || command.isServerOnly() || !command.supportsSlashCommand())
                return velen;

            if (!indexMap.containsKey(command.getName().toLowerCase()))
                return velen;

            indexes.put(indexMap.get(command.getName().toLowerCase()), command);
            return velen;
        }

        @Override
        public CompletableFuture<Void> index(DiscordApi api) {
            logger.info("Attempting to index all commands...");
            long start = System.currentTimeMillis();

            return api.getGlobalSlashCommands().thenAcceptAsync(slashCommands -> {
                Map<String, Long> newIndexes = new HashMap<>();

                slashCommands.forEach(slashCommand -> newIndexes.put(slashCommand.getName().toLowerCase(),
                        slashCommand.getId()));

                // store for any future slash commands that will try to use.
                indexMap = newIndexes;

                if (commands.isEmpty()) {
                    logger.warn("No command was found in the registry, indexing couldn't continue.");
                    return;
                }

                // start indexing.
                commands.stream()
                        .filter(VelenCommand::supportsSlashCommand)
                        .filter(command -> !command.isServerOnly())
                        .forEach(velenCommand -> indexes.put(newIndexes.get(velenCommand.getName().toLowerCase()), velenCommand));

                logger.info("All commands are now indexed. It took {} milliseconds.", System.currentTimeMillis() - start);
            });
        }

    }

    public static class Warehouse {
        public final Map<String, VelenMiddleware> middlewares = new HashMap<>();
        public final Map<String, VelenAfterware> afterwares = new HashMap<>();


        /**
         * Adds a middleware to the resources of Velen.
         * @param name The name of the middleware.
         * @param middleware The middleware component.
         */
        public void addMiddleware(String name, VelenMiddleware middleware) {
            middlewares.put(name, middleware);
        }

        /**
         * Adds an afterware to the resources of Velen.
         * @param name The name of the afterware.
         * @param afterware The afterware component.
         */
        public void addAfterware(String name, VelenAfterware afterware) {
            afterwares.put(name.toLowerCase(), afterware);
        }

        /**
         * Gets the middleware that with the specified key.
         * @param name The name of the middleware.
         * @return The middleware with the specified key.
         */
        public VelenMiddleware getMiddleware(String name) {
            return middlewares.get(name.toLowerCase());
        }

        /**
         * Gets the afterware with the specified key.
         * @param name The name of the afterware.
         * @return The afterware with the specified key.
         */
        public VelenAfterware getAfterware(String name) {
            return afterwares.get(name.toLowerCase());
        }

    }

    public static class HandlerStorage {
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
