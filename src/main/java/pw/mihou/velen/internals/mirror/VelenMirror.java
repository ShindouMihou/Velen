package pw.mihou.velen.internals.mirror;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;
import pw.mihou.velen.builders.VelenCommandBuilder;
import pw.mihou.velen.impl.VelenImpl;
import pw.mihou.velen.interfaces.*;
import pw.mihou.velen.internals.mirror.extensions.VelenBaseParser;
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
public class VelenMirror extends VelenBaseParser {

    private final VelenImpl velen;

    /**
     * Creates a new Velen Mirror to look at everyday.
     *
     * @param velen The velen instance.
     */
    public VelenMirror(Velen velen) {
        super("command");
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
            AtomicReference<STATE> state = new AtomicReference<>(STATE.DEFAULT);
            AtomicReference<String> currentSubcommand = new AtomicReference<>("none");
            AtomicReference<String> currentOption = new AtomicReference<>("none");
            AtomicReference<String> currentSubcommandGroup = new AtomicReference<>("none");
            Map<String, List<String>> choices = new HashMap<>();

            reader.lines().forEach(s -> {
                s = s.trim();

                line.incrementAndGet();

                // -------------
                // CATEGORY MANAGEMENT
                // This is where we handle all the possible categories of an entity.
                // -------------

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
                        state.set(STATE.SUBCOMMAND);
                        currentSubcommand.set(innerName);

                        // We only need the inner name for subcommands
                        // since it will just be appended to the group or the command
                        // if there isn't any group.
                        formats.put("subcommand."+innerName, new StringBuilder(innerName));
                        optionBuilders.put("subcommand."+innerName, new SlashCommandOptionBuilder()
                                .setName(innerName).setType(SlashCommandOptionType.SUB_COMMAND));
                    }

                    if (type.equalsIgnoreCase("subcommand_group")) {
                        state.set(STATE.SUBCOMMAND_GROUP);
                        currentSubcommandGroup.set(innerName);

                        // Same for subcommand group, but it will just be appended to command.
                        optionBuilders.put("group."+innerName, new SlashCommandOptionBuilder()
                                .setName(innerName).setType(SlashCommandOptionType.SUB_COMMAND_GROUP));
                    }

                    if (type.equalsIgnoreCase("option")) {
                        state.set(STATE.OPTION);
                        currentOption.set(innerName);
                        formats.put("option."+innerName, new StringBuilder(":["+innerName));
                        optionBuilders.put("option."+innerName, new SlashCommandOptionBuilder()
                                .setName(innerName));
                        choices.put(innerName, new ArrayList<>());
                    }
                }

                if (!s.startsWith("&[") && s.split("\\s+")[0].endsWith(":")) {
                    String innerName = s.split("\\s+")[0].replaceFirst(":", "").replaceFirst("\\{", "").trim();
                    String value = s.split("\\s+", 2)[1];

                    if (state.get().equals(STATE.SUBCOMMAND) || state.get().equals(STATE.SUBCOMMAND_GROUP)) {
                        String token = state.get().equals(STATE.SUBCOMMAND) ? "subcommand." : "group.";

                        // -------------
                        // COMMAND SUBCOMMAND OR SUBCOMMAND GROUP MANAGEMENT
                        // This is where we handle all the fields of an subcommand or subcommand group.
                        // -------------

                        if (innerName.equalsIgnoreCase("desc")) {
                            optionBuilders.get(token + (state.get().equals(STATE.SUBCOMMAND) ? currentSubcommand.get() : currentSubcommandGroup.get())).setDescription(value);
                        }
                    }

                    if (state.get().equals(STATE.OPTION)) {
                        SlashCommandOptionBuilder o = optionBuilders.get("option."+currentOption.get());

                        // -------------
                        // COMMAND OPTION MANAGEMENT
                        // This is where we handle all the fields of an option.
                        // -------------

                        switch(innerName.toLowerCase()) {
                            case "type": {
                                o.setType(inferFrom(value));
                                formats.get("option." + currentOption.get()).append(":of(").append(value).append(")");
                                break;
                            }
                            case "required": {
                                o.setRequired(Boolean.parseBoolean(value));
                                if (Boolean.parseBoolean(value)) {
                                    formats.get("option." + currentOption.get()).append(":required()");
                                }
                                break;
                            }
                            case "desc":
                                o.setDescription(value);
                                break;
                            case "regex":
                                formats.get("option." + currentOption.get()).append(":{").append(value).append("}");
                                break;
                            case "has_many": {
                                if (Boolean.parseBoolean(value)) {
                                    formats.get("option." + currentOption.get()).append(":hasMany()");
                                }
                                break;
                            }
                            case "choice": {
                                if (commandType.get().equalsIgnoreCase("message")) {
                                    formats.get("option." + currentOption.get()).append("::(")
                                            .append(String.join(",", array(value))).append(")");
                                } else {
                                    Pair<String, String> details = arrayOfTwo(value);
                                    choices.get(currentOption.get()).add(details.getLeft());
                                    o.addChoice(details.getLeft(), details.getRight());
                                }
                                break;
                            }
                        }
                    }

                    if (state.get().equals(STATE.DEFAULT)) {
                        // -------------
                        // COMMAND FIELD MANAGEMENT
                        // This is where we handle all the fields of a command.
                        // -------------

                        switch (innerName.toLowerCase()) {
                            case "usage":
                                builder.get().addUsage(value);
                                break;
                            case "has_format":
                                builder.get().addFormats(value);
                                break;
                            case "default_permission": {
                                if (!(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))) {
                                    throw new IllegalStateException("The value for default permission in " + commandName.get() + " is invalid, it must be of boolean type.");
                                }

                                builder.get().setDefaultPermission(Boolean.parseBoolean(value));
                                break;
                            }
                            case "desc":
                                builder.get().setDescription(value);
                                break;
                            case "middleware":
                                builder.get().addMiddlewares(value);
                                break;
                            case "middlewares":
                                builder.get().addMiddlewares(array(value));
                                break;
                            case "afterware":
                                builder.get().addAfterwares(value);
                                break;
                            case "afterwares":
                                builder.get().addAfterwares(array(value));
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
                            case "category":
                                builder.get().setCategory(value);
                                break;
                        }
                    }
                }

                // -------------
                // CLOSURE MANAGEMENT
                // This is where we handle the closures for each state.
                // -------------

                if (s.equals("}")) {
                    if (state.get().equals(STATE.SUBCOMMAND)) {
                        if (!currentSubcommandGroup.get().equalsIgnoreCase("none")) {
                            optionBuilders.get("group."+currentSubcommandGroup.get())
                                    .addOption(optionBuilders.get("subcommand."+currentSubcommand.get()).build());

                            // Append the format for this subcommand then erase.
                            formats.put("group."+currentSubcommandGroup.get()+"."+currentSubcommand.get(), new StringBuilder()
                                    .append(currentSubcommandGroup.get()).append(" ").append(formats.get("subcommand."+currentSubcommand.get())
                                            .toString()));
                            formats.remove("subcommand."+currentSubcommand.get());

                            optionBuilders.remove("subcommand."+currentSubcommand.get());
                            state.set(STATE.SUBCOMMAND_GROUP);
                        } else {
                            state.set(STATE.DEFAULT);
                        }

                        // Make sure to remove them so it doesn't get caught up in the finishing blow.
                        currentSubcommand.set("none");
                    } else if(state.get().equals(STATE.OPTION)) {
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
                            state.set(STATE.SUBCOMMAND);
                        } else {
                            formats.get("option."+currentOption.get()).append("]");
                            state.set(STATE.DEFAULT);
                        }

                        currentOption.set("none");
                    } else if (state.get().equals(STATE.SUBCOMMAND_GROUP)) {
                        state.set(STATE.DEFAULT);
                        currentSubcommandGroup.set("none");
                    } else if (state.get().equals(STATE.DEFAULT)) {
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
                        currentSubcommand.set("none");
                        currentOption.set("none");
                        currentSubcommandGroup.set("none");
                        choices.clear();
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the following file: ["+file.getPath()+"].");
        }
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


    /**
     * States are what tells us what moment we are parsing right now.
     */
    private enum STATE {
        /**
         * Default which means it is not inside any option or subcommand.
         */
        DEFAULT(0),
        /**
         * Subcommand which indicates we are inside a subcommand.
         */
        SUBCOMMAND(1),
        /**
         * Subcommand group which indicates we are inside a subcommand group.
         */
        SUBCOMMAND_GROUP(2),
        /**
         * Option which indicates we are inside an option.
         */
        OPTION(3);

        private int value;

        STATE(int value) {
            this.value = value;
        }

        /**
         * Retrieves the value for this state.
         * @return The value of this state.
         */
        public int getValue() {
            return value;
        }
    }

}
