package pw.mihou.velen.internals.observer;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.internals.observer.modes.ObserverMode;
import pw.mihou.velen.utils.Pair;
import pw.mihou.velen.utils.VelenThreadPool;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Velen Observer is the newer equivalent of the previous Slash Command Checker which
 * is now deprecated and scheduled for removal for various of reasons. The observer is
 * far more advanced and offers better performance than the checker in checking whether
 * a command needs to be updated on the API.
 */
public class VelenObserver {

    private static final Logger logger = LoggerFactory.getLogger("Velen - Observer");
    private final DiscordApi api;
    private final ObserverMode mode;

    /**
     * This creates a new {@link VelenObserver} which observes for any changes
     * on the commands on start-up and updates them, register them or only log them
     * depending on the {@link ObserverMode} selected.
     *
     * @param api  Any single Discord API Instance used to communicate with Discord, you don't have to use
     *             multiple shards. All you need is one shard, for example, shard 0 if you are sharding.
     * @param mode The {@link ObserverMode} to use for this instance.
     */
    public VelenObserver(DiscordApi api, ObserverMode mode) {
        this.api = api;
        this.mode = mode;
    }

    /**
     * Retrieves and performs an observation check for all
     * slash commands of all servers in the shards specified. <b>DO THIS AS YOUR OWN RISK.</b>.
     *
     * Not to be confused as {@link VelenObserver#observeServer(Velen, DiscordApi...)} which only checks
     * for all the commands registered in the registry, disabling the automatic removal functionality.
     *
     * @param velen The Velen instance to fetch commands.
     * @param shards The shards to fetch all the slash commands.
     * @return A future that indicates progress or completion.
     */
    public CompletableFuture<Void> observeAllServers(Velen velen, DiscordApi... shards) {
        return CompletableFuture.allOf(Arrays.stream(shards).map(DiscordApi::getServers)
                .map(servers -> CompletableFuture.allOf(servers.stream().map(server -> observeServer(velen, server))
                        .toArray(CompletableFuture[]::new)))
                .toArray(CompletableFuture[]::new));
    }

    /**
     * Retrieves and performs an observation check for all slash commands
     * in the specified server.
     *
     * @param velen The Velen instance to check.
     * @param server The server to fetch the commands from.
     * @return A future that indicates progress.
     */
    public CompletableFuture<Void> observeServer(Velen velen,  Server server) {
        List<VelenCommand> commands = velen.getCommands()
                .stream()
                .filter(VelenCommand::supportsSlashCommand)
                .filter(VelenCommand::isServerOnly)
                .filter(s -> s.asSlashCommand().getLeft() != 0L && s.asSlashCommand().getLeft() != null && s.asSlashCommand().getLeft() == server.getId())
                .collect(Collectors.toList());

        return server.getSlashCommands().thenAcceptAsync(slashCommands -> commands.forEach(velenCommand -> finalizeServer(server, slashCommands, commands)));
    }

    /**
     * Retrieves and performs an observation check for all
     * slash commands of all the servers <b>THAT HAS A COMMAND ATTACHED TO IT</b> on the Velen registry.
     *
     * Not to be confused with {@link VelenObserver#observeAllServers(Velen, DiscordApi...)} which checks all
     * servers' slash commands which allows that method to be both risky and also support all functionalities such as
     * deleting slash commands that are no longer registered in Velen.
     *
     * @param velen The Velen instance to fetch commands.
     * @param apis The shards to fetch all the slash commands.
     * @return A future that indicates progress or completion.
     */
    public CompletableFuture<Void> observeServer(Velen velen, DiscordApi... apis) {
        List<DiscordApi> shards = Arrays.stream(apis)
                .sorted(Comparator.comparingInt(DiscordApi::getCurrentShard))
                .collect(Collectors.toList());

        List<VelenCommand> commands = velen.getCommands()
                .stream()
                .filter(VelenCommand::supportsSlashCommand)
                .filter(VelenCommand::isServerOnly)
                .filter(s -> s.asSlashCommand().getLeft() != 0L && s.asSlashCommand().getLeft() != null)
                .collect(Collectors.toList());

        // We need to store the received lists of all the servers
        // temporarily to reduce requests.
        Map<Long, List<SlashCommand>> serverSlashCommands = new HashMap<>();

        // We are executing something non-asynchronous inside
        // so we need to run this entire thing outside the current thread.
        return CompletableFuture.runAsync(() -> commands.forEach(v -> {
            Pair<Long, SlashCommandBuilder> pair = v.asSlashCommand();

            Server server = shards.stream()
                    .filter(discordApi -> discordApi.getServerById(pair.getLeft()).isPresent())
                    .map(discordApi -> discordApi.getServerById(pair.getLeft())
                            .orElseThrow(() -> new NullPointerException("There is something fishy here, please report to Javacord issues.")))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The command " + v.getName()
                            + "'s server " + pair.getLeft() + " cannot be found through all " + shards.get(0).getTotalShards() + " shards."));

            if (!serverSlashCommands.containsKey(pair.getLeft())) {
                serverSlashCommands.put(pair.getLeft(), api.getServerSlashCommands(server).join());
            }

            List<SlashCommand> slashCommands = serverSlashCommands.get(pair.getLeft());

            finalizeServer(server, slashCommands, commands);
        }), VelenThreadPool.executorService);
    }

    /**
     * This performs a global observation check that compares all slash commands
     * globally with what is registered on Velen's registry.
     *
     * @param velen The velen instance to check.
     * @return A future to indicate progress.
     */
    public CompletableFuture<Void> observe(Velen velen) {
        List<VelenCommand> commands = velen.getCommands()
                .stream()
                .filter(VelenCommand::supportsSlashCommand)
                .filter(s -> s.asSlashCommand().getLeft() == 0L || s.asSlashCommand().getLeft() == null)
                .collect(Collectors.toList());

        return api.getGlobalSlashCommands().thenAcceptAsync(slashCommands -> {

            if (mode.isCreate()) {
                existentialFilter(commands, slashCommands).forEach(command -> {
                    long start = System.currentTimeMillis();
                    command.asSlashCommand().getRight().createGlobal(api).thenAccept(slashCommand ->
                            logger.info("Application command was created. [name={}, description={}, id={}]. It took {} milliseconds.", slashCommand.getName(), slashCommand.getDescription(),
                                    slashCommand.getId(), System.currentTimeMillis() - start))
                        .exceptionally(ExceptionLogger.get());
                });
            }

            if (mode.isUpdate()) {
                crustFilter(commands, slashCommands).forEach((aLong, velenCommand) -> {
                    long start = System.currentTimeMillis();

                    velenCommand.asSlashCommandUpdater(aLong).getRight().updateGlobal(api)
                                    .thenAccept(slashCommand -> logger.info("Application command was updated. [name={}, description={}, id={}]. It took {} milliseconds.",
                                        slashCommand.getName(), slashCommand.getDescription(), slashCommand.getId(), System.currentTimeMillis() - start))
                        .exceptionally(ExceptionLogger.get());
                });
            }

            if (!mode.isUpdate() && !mode.isCreate()) {
                existentialFilter(commands, slashCommands).forEach(command -> logger.warn("Application command is not registered on Discord API. [{}]", command.toString()));
                crustFilter(commands, slashCommands).forEach((aLong, velenCommand) ->
                        logger.warn("Application command requires updating. [id={}, {}]", aLong, velenCommand.toString()));
            }

        });
    }


    /**
     * This performs the filter to check if a slash command is registered
     * or not in the Discord API.
     *
     * @param commands The commands to validate.
     * @param slashCommands The slash commands registered to compare.
     * @return A list of commands that haven't been registered to the Discord API.
     */
    private List<VelenCommand> existentialFilter(List<VelenCommand> commands, List<SlashCommand> slashCommands) {
        return commands.stream()
                .filter(s -> slashCommands.stream()
                        .map(SlashCommand::getName)
                        .noneMatch(s1 -> s1.equalsIgnoreCase(s.getName())))
                .collect(Collectors.toList());
    }

    /**
     * This performs the filter to check if a slash command is registered
     * or not in the Discord API.
     *
     * @param commands The commands to validate.
     * @param slashCommands The slash commands registered to compare.
     * @return A list of commands that have been registered to the Discord API.
     */
    private List<VelenCommand> existingFilter(List<VelenCommand> commands, List<SlashCommand> slashCommands) {
        return commands.stream()
                .filter(s -> slashCommands.stream()
                        .map(SlashCommand::getName)
                        .anyMatch(s1 -> s1.equalsIgnoreCase(s.getName())))
                .collect(Collectors.toList());
    }

    /**
     * This performs a filter that maps out the first depth differences
     * of the already existing commands and validates whether any has changed.
     *
     * @param commands The commands to validate.
     * @param slashCommands The slash commands registered to compare.
     * @return A list of commands that needs to be updated in the Discord API.
     */
    private Map<Long, VelenCommand> crustFilter(List<VelenCommand> commands, List<SlashCommand> slashCommands) {
        List<VelenCommand> commandList = existingFilter(commands, slashCommands);
        
        if (commandList.isEmpty())
            return Collections.emptyMap();

        AtomicReference<Map<Long, VelenCommand>> reference = new AtomicReference<>(new HashMap<>());
        commandList.forEach(command -> {
            SlashCommand slashCommand = slashCommands.stream()
                    .filter(s -> s.getName().equalsIgnoreCase(command.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("The observer was unable to find a command that is supposed to match, did a bit-flip happen?"));

            if (!command.getDescription().equalsIgnoreCase(slashCommand.getDescription())) {
                reference.get().put(slashCommand.getId(), command);
                return;
            }

            if (slashCommand.getDefaultPermission() != command.isDefaultPermissionEnabled()) {
                reference.get().put(slashCommand.getId(), command);
                return;
            }

            reference.set(depthFilter(command, slashCommand, reference.get(), slashCommand.getOptions(), command.getOptions()));
        });

        return reference.get();
    }

    /**
     * This performs a filter that checks the options, choices and other
     * deeper levels of two commands and compares them.
     *
     * @param command The command to check.
     * @param slashCommand The slash command equivalent to check.
     * @param differences The differences in the two.
     * @param slashCommandOptions The options to check.
     * @param velenCommandOptions The velen options to check.
     * @return
     */
    private Map<Long, VelenCommand> depthFilter(VelenCommand command, SlashCommand slashCommand, Map<Long, VelenCommand> differences,
                                           List<SlashCommandOption> slashCommandOptions, List<SlashCommandOption> velenCommandOptions) {

        // This checks if the slash command option is present on Discord API
        // but removed on Velen.
        slashCommandOptions.forEach(slashCommandOption -> {
            Optional<SlashCommandOption> velenOptional = velenCommandOptions.stream()
                    .filter(o -> o.getName().equalsIgnoreCase(slashCommandOption.getName()))
                    .filter(o -> o.getDescription().equals(slashCommandOption.getDescription()))
                    .filter(o -> o.isRequired() == slashCommandOption.isRequired())
                    .filter(o -> o.getType().getValue() == slashCommandOption.getType().getValue())
                    .findFirst();

            if (!velenOptional.isPresent()) {
                differences.put(slashCommand.getId(), command);
            }
        });

        velenCommandOptions.forEach(slashCommandOption -> {
            Optional<SlashCommandOption> velenOptional = slashCommandOptions.stream()
                    .filter(o -> o.getName().equalsIgnoreCase(slashCommandOption.getName()))
                    .filter(o -> o.getDescription().equals(slashCommandOption.getDescription()))
                    .filter(o -> o.isRequired() == slashCommandOption.isRequired())
                    .filter(o -> o.getType().getValue() == slashCommandOption.getType().getValue())
                    .findFirst();

            if (!velenOptional.isPresent()) {
                differences.put(slashCommand.getId(), command);
                return;
            }

            SlashCommandOption option = velenOptional.get();

            // Time to start checking the choices for any differences.
            option.getChoices().forEach(choice -> {
                Optional<SlashCommandOptionChoice> oChoice = slashCommandOption
                        .getChoices().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(choice.getName()))
                        .filter(c -> c.getLongValue().isPresent() == choice.getLongValue().isPresent())
                        .filter(c -> c.getStringValue().isPresent() == choice.getStringValue().isPresent())
                        .filter(c -> c.getValueAsString().equalsIgnoreCase(choice.getValueAsString()))
                        .findFirst();

                if (!oChoice.isPresent()) {
                    differences.put(slashCommand.getId(), command);
                    return;
                }

                SlashCommandOptionChoice kO = oChoice.get();

                if ((kO.getLongValue().isPresent() && choice.getLongValue().isPresent())
                        && (kO.getLongValue().get().equals(choice.getLongValue().get()))) {
                    differences.put(slashCommand.getId(), command);
                    return;
                }

                if (kO.getStringValue().isPresent() && choice.getStringValue().isPresent()
                        && !kO.getStringValue().get().equalsIgnoreCase(choice.getStringValue().get())) {
                    differences.put(slashCommand.getId(), command);
                }
            });

            // Break off from the repeated loop after this.
            if (option.getOptions().isEmpty() && slashCommandOption.getOptions().isEmpty()) {
                return;
            }

            // We'll have to generate a new HashMap to prevent duplication.
            differences.putAll(depthFilter(command, slashCommand, new HashMap<>(),
                    option.getOptions(), slashCommandOption.getOptions()));
        });

        return differences;
    }

    /**
     * This performs finalization on server-command checks for Velen.
     *
     * @param server The server to check.
     * @param slashCommands The slash commands to check.
     * @param commands The commands to check.
     */
    private void finalizeServer(Server server, List<SlashCommand> slashCommands, List<VelenCommand> commands) {
        if (mode.isCreate()) {
            existentialFilter(commands, slashCommands).forEach(command -> {
                long start = System.currentTimeMillis();
                command.asSlashCommand().getRight().createForServer(server).thenAccept(slashCommand ->
                                logger.info("Application command was created for server {}. [name={}, description={}, id={}]. It took {} milliseconds.",
                                        server.getId(),
                                        slashCommand.getName(), slashCommand.getDescription(),
                                        slashCommand.getId(), System.currentTimeMillis() - start))
                        .exceptionally(ExceptionLogger.get());
            });
        }

        if (mode.isUpdate()) {
            crustFilter(commands, slashCommands).forEach((aLong, velenCommand) -> {
                long start = System.currentTimeMillis();

                velenCommand.asSlashCommandUpdater(aLong).getRight().updateForServer(server)
                        .thenAccept(slashCommand -> logger.info("Application command was updated for server {}. [name={}, description={}, id={}]. It took {} milliseconds.",
                                server.getId(), slashCommand.getName(), slashCommand.getDescription(), slashCommand.getId(), System.currentTimeMillis() - start))
                        .exceptionally(ExceptionLogger.get());
            });
        }

        if (!mode.isUpdate() && !mode.isCreate()) {
            existentialFilter(commands, slashCommands).forEach(command -> logger.warn("Application command is not registered on Discord API. [{}]", command.toString()));
            crustFilter(commands, slashCommands).forEach((aLong, velenCommand) ->
                    logger.warn("Application command requires updating. [id={}, {}]", aLong, velenCommand.toString()));
        }
    }

}
