package pw.mihou.velen.internals.observer;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.javacord.api.util.logging.ExceptionLogger;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.internals.observer.modes.ObserverMode;

import java.util.*;
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

    public void observe(Velen velen) {
        List<VelenCommand> commands = velen.getCommands()
                .stream()
                .filter(VelenCommand::supportsSlashCommand)
                .filter(s -> s.asSlashCommand().getLeft() == 0L || s.asSlashCommand().getLeft() == null)
                .collect(Collectors.toList());

        api.getGlobalSlashCommands().thenAcceptAsync(slashCommands -> {

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

}
