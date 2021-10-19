package pw.mihou.velen.internals.mirror;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;
import pw.mihou.velen.builders.VelenCommandBuilder;
import pw.mihou.velen.impl.VelenImpl;
import pw.mihou.velen.interfaces.*;
import pw.mihou.velen.internals.routing.VelenUnderscoreParser;
import pw.mihou.velen.utils.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
public class VelenMirror {

    private final VelenImpl velen;

    /**
     * Creates a new Velen Mirror to look at everyday.
     *
     * @param velen The velen instance.
     */
    public VelenMirror(Velen velen) {
        this.velen = (VelenImpl) velen;
    }

    public void comprehend(File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            AtomicReference<VelenCommandBuilder> builder = new AtomicReference<>(new VelenCommandBuilder());
            AtomicReference<String> commandType = new AtomicReference<>("inferred");
            AtomicReference<String> commandName = new AtomicReference<>("Unidentified");
            AtomicInteger line = new AtomicInteger(0);

            Map<String, SlashCommandOptionBuilder> optionBuilders = new HashMap<>();
            Map<String, StringBuilder> formats = new HashMap<>();

            // States are used to tell us in what moment we are on right now.
            // 0 = default (not inside an option or subcommand).
            // 1 = inside a subcommand.
            // 2 = inside a subcommand group.
            // 3 = inside an option.
            AtomicInteger state = new AtomicInteger();

            AtomicReference<String> currentSubcommand = new AtomicReference<>("none");
            AtomicReference<String> currentOption = new AtomicReference<>("none");
            AtomicReference<String> currentSubcommandGroup = new AtomicReference<>("none");

            Map<String, List<String>> choices = new HashMap<>();

            reader.lines().forEach(s -> {
                s = s.trim();

                line.incrementAndGet();
                if (s.startsWith("&[") && s.contains("]: ")) {
                    Pair<Integer, Integer> positions = VelenUnderscoreParser.find("&[", ']', s);
                    String innerName = s.substring(positions.getLeft() + 2, positions.getRight()).trim();

                    String type = s.substring(positions.getRight()+2).trim();

                    if (type.endsWith("{"))
                        type = type.substring(0, type.length() - 1).trim();

                    if (type.equalsIgnoreCase("category")) {
                        builder.get().setCategory(innerName);
                    }

                    if (type.equalsIgnoreCase("message") || type.equalsIgnoreCase("slash")
                            || type.equalsIgnoreCase("hybrid")) {
                        // Store these for access later.
                        commandType.set(type);
                        commandName.set(innerName);

                        builder.get().setName(innerName);
                    }

                    if (type.equalsIgnoreCase("subcommand")) {
                        state.set(1);

                        // Save for future reference.
                        currentSubcommand.set(innerName);

                        // We only need the inner name for subcommands
                        // since it will just be appended to the group or the command
                        // if there isn't any group.
                        formats.put("subcommand."+innerName, new StringBuilder(innerName));
                        optionBuilders.put("subcommand."+innerName, new SlashCommandOptionBuilder()
                                .setName(innerName).setType(SlashCommandOptionType.SUB_COMMAND));
                    }

                    if (type.equalsIgnoreCase("subcommand_group")) {
                        state.set(2);
                        currentSubcommandGroup.set(innerName);

                        // Same for subcommand group, but it will just be appended to command.
                        optionBuilders.put("group."+innerName, new SlashCommandOptionBuilder()
                                .setName(innerName).setType(SlashCommandOptionType.SUB_COMMAND_GROUP));
                    }

                    if (type.equalsIgnoreCase("option")) {
                        state.set(3);
                        currentOption.set(innerName);
                        formats.put("option."+innerName, new StringBuilder(":["+innerName));
                        optionBuilders.put("option."+innerName, new SlashCommandOptionBuilder()
                                .setName(innerName));
                        choices.put(innerName, new ArrayList<>());
                    }
                }

                // Both subcommands and subcommand groups have the same outline.
                if (!s.startsWith("&[") && s.split("\\s+")[0].endsWith(":") && (state.get() == 1 || state.get() == 2)) {
                    String innerName = s.split("\\s+")[0].replaceFirst(":", "")
                            .replaceFirst("\\{", "").trim();
                    String value = s.split("\\s+", 2)[1];

                    String token = state.get() == 1 ? "subcommand." : "group.";
                    if (innerName.equalsIgnoreCase("desc")) {
                        optionBuilders.get(token+(state.get() == 1 ? currentSubcommand.get() : currentSubcommandGroup.get())).setDescription(value);
                    }
                }

                if (!s.startsWith("&[") && s.split("\\s+")[0].endsWith(":") && state.get() == 3) {
                    String innerName = s.split("\\s+")[0].replaceFirst(":", "")
                            .replaceFirst("\\{", "").trim();
                    String value = s.split("\\s+", 2)[1];

                    SlashCommandOptionBuilder o = optionBuilders.get("option."+currentOption.get());
                    if (innerName.equalsIgnoreCase("type")) {
                        o.setType(inferFrom(value));
                        formats.get("option." + currentOption.get()).append(":of(").append(value).append(")");
                    }

                    if (innerName.equalsIgnoreCase("required")) {
                        o.setRequired(Boolean.parseBoolean(value));
                        if (Boolean.parseBoolean(value)) {
                            formats.get("option." + currentOption.get()).append(":required()");
                        }
                    }

                    if (innerName.equalsIgnoreCase("desc")) {
                        o.setDescription(value);
                    }

                    if (innerName.equalsIgnoreCase("regex")) {
                        formats.get("option." + currentOption.get()).append(":{").append(value).append("}");
                    }

                    if (innerName.equalsIgnoreCase("has_many")) {
                        if (Boolean.parseBoolean(value)) {
                            formats.get("option." + currentOption.get()).append(":hasMany()");
                        }
                    }

                    if (innerName.equalsIgnoreCase("choice")) {
                        if (commandType.get().equalsIgnoreCase("message")) {
                            formats.get("option." + currentOption.get()).append("::(")
                                    .append(String.join(",", array(value))).append(")");
                        } else {
                            Pair<String, String> details = arrayOfTwo(value);
                            choices.get(currentOption.get()).add(details.getLeft());
                            o.addChoice(details.getLeft(), details.getRight());
                        }
                    }
                }

                // If it is a subcommand, then just set the state to zero if it isn't in a group.
                // and 2 if it is on a group.
                if (s.equals("}") && state.get() == 1) {
                    if (!currentSubcommandGroup.get().equalsIgnoreCase("none")) {
                        optionBuilders.get("group."+currentSubcommandGroup.get())
                                .addOption(optionBuilders.get("subcommand."+currentSubcommand.get()).build());

                        // Append the format for this subcommand then erase.
                        formats.put("group."+currentSubcommandGroup.get()+"."+currentSubcommand.get(), new StringBuilder()
                                .append(currentSubcommandGroup.get()).append(" ").append(formats.get("subcommand."+currentSubcommand.get())
                                        .toString()));
                        formats.remove("subcommand."+currentSubcommand.get());

                        optionBuilders.remove("subcommand."+currentSubcommand.get());
                        state.set(2);
                    } else {
                        state.set(0);
                    }

                    // Make sure to remove them so it doesn't get caught up in the finishing blow.
                    currentSubcommand.set("none");
                } else if (s.equals("}") && state.get() == 3) {
                    if (!choices.get(currentOption.get()).isEmpty()) {
                        formats.get("option." + currentOption.get()).append("::(")
                                .append(String.join(",", choices.get(currentOption.get())))
                                .append(")");
                        choices.remove(currentOption.get());
                    }

                    if (!currentSubcommand.get().equalsIgnoreCase("none")) {
                        optionBuilders.get("subcommand."+currentSubcommand.get())
                                .addOption(optionBuilders.get("option."+currentOption.get()).build());

                        // And this option is done.
                        formats.get("subcommand."+currentSubcommand.get()).append(" ")
                                .append(formats.get("option."+currentOption.get()).append("]"));
                        formats.remove("option."+currentOption.get());

                        // Make sure to remove them, so it doesn't get caught up in the finishing blow.
                        optionBuilders.remove("option."+currentOption.get());
                        state.set(1);
                    } else {
                        formats.get("option."+currentOption.get()).append("]");
                        state.set(0);
                    }

                    currentOption.set("none");
                } else if (s.equals("}") && state.get() == 2) {
                    state.set(0);
                    currentSubcommandGroup.set("none");
                } else if (s.equals("}") && state.get() == 0) {
                    formats.forEach((s1, stringBuilder) -> builder.get().addFormats(commandName.get() + " " + stringBuilder.toString()));

                    // If there are no optiosn that are required, then we need to have Velen know that.
                    if (formats.values().stream().noneMatch(b -> b.toString().contains("::required()"))) {
                        builder.get().addFormats(commandName.get());
                    }

                    optionBuilders.values().forEach(builder.get()::addOption);
                    builder.get().setVelen(velen).attach();

                    // Let me introduce to you...
                    // the wiper.

                    builder.set(new VelenCommandBuilder());
                    commandType.set("inferred");
                    commandName.set("Unidentified");
                    optionBuilders.clear();
                    formats.clear();
                    state.set(0);
                    currentSubcommand.set("none");
                    currentOption.set("none");
                    currentSubcommandGroup.set("none");
                    choices.clear();
                }

                if (!s.startsWith("&[") && s.split("\\s+")[0].trim().endsWith(":") && state.get() == 0) {
                    String innerName = s.split("\\s+")[0].replaceFirst(":", "").trim();
                    String value = s.split("\\s+", 2)[1].trim();
                    switch (innerName.toLowerCase()) {
                        case "usage":
                            builder.get().addUsage(value);
                            break;
                        case "has_format":
                            builder.get().addFormats(value);
                            break;
                        case "desc":
                            builder.get().setDescription(value);
                            break;
                        case "shortcut":
                            builder.get().addShortcut(value);
                            break;
                        case "handler": {
                            if (!velen.getHandlerStorage().contains(value)) {
                                throw new IllegalStateException("Failed to create command from the following file: ["+file.getPath()+"] with name " +
                                        "["+commandName.get()+"]: There is no handler with name of ["+value+"], please read the wiki for more information.");
                            }

                            switch (commandType.get().toLowerCase()) {
                                case "slash": {
                                    Optional<VelenSlashEvent> event = velen.getHandlerStorage().findSlashHandlers(value.toLowerCase());
                                    if (!event.isPresent()) {
                                        throw error(file, commandName.get(), "There are no slash command handlers available with name of ["+value+"]");
                                    }

                                    builder.get().setSlashEvent(event.get());
                                    break;
                                }
                                case "message": {
                                    Optional<VelenEvent> event = velen.getHandlerStorage().findMessageHandler(value.toLowerCase());
                                    if (!event.isPresent()) {
                                        throw error(file, commandName.get(), "There are no message command handlers available with name of ["+value+"]");
                                    }

                                    builder.get().doEventOnInvocation(event.get());
                                    break;
                                }
                                case "hybrid":
                                case "inferred": {
                                    Optional<VelenHybridHandler> hybridHandler = velen.getHandlerStorage().findHybridHandlers(value.toLowerCase());
                                    Optional<VelenEvent> messageHandler = velen.getHandlerStorage().findMessageHandler(value.toLowerCase());
                                    Optional<VelenSlashEvent> slashHandler = velen.getHandlerStorage().findSlashHandlers(value.toLowerCase());
                                    if (!hybridHandler.isPresent() && !messageHandler.isPresent() && !slashHandler.isPresent()) {
                                        throw error(file, commandName.get(), "There are no command handlers available with name of ["+value+"]");
                                    }

                                    hybridHandler.ifPresent(h -> builder.get().setHybridHandler(h));
                                    messageHandler.ifPresent(m -> builder.get().doEventOnInvocation(m));
                                    slashHandler.ifPresent(r -> builder.get().setSlashEvent(r));
                                    break;
                                }
                            }
                            break;
                        }
                        case "server_only": {
                            Pair<String, String> val = arrayOfTwo(value);

                            if (val.getRight() == null) {
                                builder.get().setServerOnly(Boolean.parseBoolean(val.getLeft()));
                            } else {
                                builder.get().setServerOnly(Boolean.parseBoolean(val.getLeft()), Long.parseLong(val.getRight()));
                            }

                            break;
                        }
                        case "dms_only":
                            builder.get().setPrivateChannelOnly(Boolean.parseBoolean(value));
                            break;
                        case "permissions":
                            builder.get().requirePermissions(Arrays.stream(array(value)).map(perms -> Arrays.stream(PermissionType.values())
                                .filter(p -> p.name().equalsIgnoreCase(perms))
                                .findFirst()
                                .orElseThrow(() -> error(file, commandName.get(), "There are no Permission Type with name of ["+perms+"]")))
                                .toArray(PermissionType[]::new));
                            break;
                        case "roles":
                            Arrays.stream(array(value)).map(Long::parseLong).forEach(builder.get()::requireRole);
                            break;
                        case "cooldown":
                            builder.get().setCooldown(Duration.ofMillis(Long.parseLong(value)));
                            break;
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the following file: ["+file.getPath()+"].");
        }
    }

    public IllegalStateException error(File file, String commandName, String error) {
        throw new IllegalStateException("Failed to create command from the following file: ["+file.getPath()+"] with name " +
                "["+commandName+"]: \n" + error + ", please read the wiki for more information.");
    }

    private String[] array(String value) {
        String[] arr;
        if (value.startsWith("[") && value.endsWith("]"))
            arr = value.substring(1, value.length() - 1).split(",");
        else
            arr = value.split(",");

        arr = Arrays.stream(arr).map(String::trim).toArray(String[]::new);
        return arr;
    }

    private SlashCommandOptionType inferFrom(String type) {
        return Arrays.stream(SlashCommandOptionType.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst().orElse(inferUsingUnderscore(type));
    }

    private SlashCommandOptionType inferUsingUnderscore(String type) {
        switch (type.toLowerCase()) {
            case "user": return SlashCommandOptionType.USER;
            case "channel": return SlashCommandOptionType.CHANNEL;
            case "role": return SlashCommandOptionType.ROLE;
            case "numeric": return SlashCommandOptionType.INTEGER;
            case "boolean": return SlashCommandOptionType.BOOLEAN;
            default: return SlashCommandOptionType.STRING;
        }
    }

    private Pair<String, String> arrayOfTwo(String value) {
        String[] array = array(value);

        return Pair.of(array[0], array.length > 1 ? array[1] : null);
    }

}
