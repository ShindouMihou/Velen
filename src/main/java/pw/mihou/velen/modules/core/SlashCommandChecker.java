package pw.mihou.velen.modules.core;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.mihou.velen.impl.VelenCommandImpl;
import pw.mihou.velen.interfaces.Velen;
import pw.mihou.velen.interfaces.VelenCommand;
import pw.mihou.velen.modules.modes.SlashCommandCheckerMode;
import pw.mihou.velen.utils.VelenThreadPool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This is used to check whether a slash command from the API needs to be updated
 * or not with the help of {@link DiscordApi#getGlobalSlashCommands()}, {@link DiscordApi#getServerSlashCommands(Server)}
 * and {@link Velen#getCommands()}.
 * <p>
 * This will only work if the command still has the same name, and this will immediately update any via global
 * instead of server-only.
 */
public class SlashCommandChecker {

    private static final Logger logger = LoggerFactory.getLogger("Velen - Slash Command Checker & Updater");
    private final DiscordApi api;
    private final SlashCommandCheckerMode mode;
    SlashCommandCheckerInternal internals = new SlashCommandCheckerInternal();


    public SlashCommandChecker(DiscordApi api, SlashCommandCheckerMode mode) {
        this.api = api;
        this.mode = mode;
    }

    /**
     * Checks for any differences between the currently registered {@link VelenCommand} on {@link Velen} instance
     * before updating or creating the command. This will only create the command if the mode set for this slash command
     * instance is {@link SlashCommandCheckerMode#NORMAL}.
     *
     * @param velen The velen instance to use.
     * @return The amount of commands updated.
     */
    public CompletableFuture<Integer> run(Velen velen) {
        AtomicInteger counter = new AtomicInteger(0);
        // To ensure that we only get global commands.
        List<VelenCommand> commands = velen.getCommands()
                .stream()
                .filter(velenCommand -> (((VelenCommandImpl) velenCommand).asSlashCommand().getLeft() == null
                        || ((VelenCommandImpl) velenCommand).asSlashCommand().getLeft() == 0L))
                .collect(Collectors.toList());

        return api.getGlobalSlashCommands()
                .thenApply(slashCommands -> CompletableFuture.supplyAsync(() -> {
                    internals.mapAllDifferences(commands, slashCommands);

                    if (!internals.getDifferences().isEmpty()) {
                        logger.info("Found {} differences (excluding not-added commands), attempting to update...", internals.getDifferences().size());
                        internals.getDifferences().forEach((aLong, velenCommand) -> velen.updateSlashCommand(aLong, velenCommand, api)
                                .thenAccept(slashCommand -> {
                                    logger.debug("Successfully updated a command! (name={}, description={})",
                                            slashCommand.getName(), slashCommand.getDescription());
                                    counter.incrementAndGet();
                                }).join());
                    }

                    if (mode == SlashCommandCheckerMode.NORMAL) {
                        internals.getAllNotRegistered(commands, slashCommands)
                                .forEach(velenCommand -> ((VelenCommandImpl) velenCommand).asSlashCommand().getRight().createGlobal(api).thenAccept(slashCommand -> {
                                    logger.debug("Successfully created a command! (name={}, description={})",
                                            slashCommand.getName(), slashCommand.getDescription());
                                    counter.incrementAndGet();
                                }).join());
                    }

                    return counter.get();
                }, VelenThreadPool.executorService)).join();
    }

    class SlashCommandCheckerInternal {

        private final Map<Long, VelenCommand> commands = new HashMap<>();

        private Map<Long, VelenCommand> getDifferences() {
            return commands;
        }

        private List<VelenCommand> getAllNotRegistered(List<VelenCommand> velenCommands, List<SlashCommand> slashCommands) {
            return velenCommands.stream().filter(velenCommand -> slashCommands.stream()
                            .noneMatch(slashCommand -> velenCommand.getName().equalsIgnoreCase(slashCommand.getName())))
                    .collect(Collectors.toList());
        }

        private void mapAllDifferences(List<VelenCommand> velenCommands, List<SlashCommand> slashCommands) {
            slashCommands.forEach(slashCommand -> {
                if (velenCommands.stream().noneMatch(velenCommand -> slashCommand.getName()
                        .equalsIgnoreCase(velenCommand.getName())))
                    return;

                velenCommands.stream().filter(velenCommand -> velenCommand.getName().equalsIgnoreCase(slashCommand.getName()))
                        .forEachOrdered(velenCommand -> {
                            if (!velenCommand.getDescription().equalsIgnoreCase(slashCommand.getDescription())) {
                                commands.put(slashCommand.getId(), velenCommand);
                                return;
                            }

                            mapAllInnerOptionsDifference(slashCommand,
                                    slashCommand.getOptions(),
                                    velenCommand.getOptions(),
                                    velenCommand);
                        });
            });
        }

        private void mapAllInnerOptionsDifference(SlashCommand slashCommand, List<SlashCommandOption> options,
                                                  List<SlashCommandOption> velenOptions, VelenCommand velenCommand) {
            options.forEach(slashCommandOption -> {
                Optional<SlashCommandOption> optOptions = velenOptions.stream()
                        .filter(o -> o.getName().equalsIgnoreCase(slashCommandOption.getName()))
                        .filter(o -> o.getDescription().equalsIgnoreCase(slashCommandOption.getDescription()))
                        .filter(o -> o.isRequired() == slashCommandOption.isRequired())
                        .filter(o -> o.getType().getValue() == slashCommandOption.getType().getValue())
                        .findFirst();

                if (!optOptions.isPresent()) {
                    commands.put(slashCommand.getId(), velenCommand);
                    return;
                }

                SlashCommandOption option = optOptions.get();

                option.getChoices().forEach(choice -> {
                    Optional<SlashCommandOptionChoice> oChoice = slashCommandOption
                            .getChoices().stream()
                            .filter(c -> c.getName().equalsIgnoreCase(choice.getName()))
                            .filter(c -> c.getIntValue().isPresent() == choice.getIntValue().isPresent())
                            .filter(c -> c.getStringValue().isPresent() == choice.getStringValue().isPresent())
                            .filter(c -> c.getValueAsString().equalsIgnoreCase(choice.getValueAsString()))
                            .findFirst();

                    if (!oChoice.isPresent()) {
                        commands.put(slashCommand.getId(), velenCommand);
                        return;
                    }

                    SlashCommandOptionChoice kO = oChoice.get();

                    if (kO.getIntValue().isPresent() && choice.getIntValue().isPresent()) {
                        if (!kO.getIntValue().get().equals(choice.getIntValue().get())) {
                            commands.put(slashCommand.getId(), velenCommand);
                            return;
                        }
                    }

                    if (kO.getStringValue().isPresent() && choice.getStringValue().isPresent()) {
                        if (!kO.getStringValue().get().equalsIgnoreCase(choice.getStringValue().get())) {
                            commands.put(slashCommand.getId(), velenCommand);
                        }
                    }
                });

                // Break from life after this.
                if (option.getOptions().isEmpty() && slashCommandOption.getOptions().isEmpty())
                    return;

                // Do another inner check if there is still more options.
                mapAllInnerOptionsDifference(slashCommand, slashCommandOption.getOptions(), option.getOptions(), velenCommand);
            });
        }

    }

}
