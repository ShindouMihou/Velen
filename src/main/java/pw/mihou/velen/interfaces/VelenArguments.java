package pw.mihou.velen.interfaces;

import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.List;
import java.util.Optional;

public class VelenArguments {

    private final List<SlashCommandInteractionOption> options;

    /**
     * Creates a new Velen Slash Command Option wrapper.
     * This wraps the slash commands in a way that makes it easier to
     * look through without having to go through an ifPresent cluster.
     *
     * @param options The options.
     */
    public VelenArguments(List<SlashCommandInteractionOption> options) {
        this.options = options;
    }

    /**
     * Gets a Slash Command option by its name.
     *
     * @param name The name of the option.
     * @return The slash command option.
     */
    public Optional<SlashCommandInteractionOption> getOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name)).findAny();
    }

    /**
     * Gets a Slash Command option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The slash command option.
     */
    public Optional<SlashCommandInteractionOption> getOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name)).findAny();
    }

    /**
     * Gets a boolean option by its name.
     *
     * @param name The name of the option.
     * @return The boolean value.
     */
    public Optional<Boolean> getBooleanOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getBooleanValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getBooleanValue().get())
                .findAny();
    }

    /**
     * Gets a boolean option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The boolean value.
     */
    public Optional<Boolean> getBooleanOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getBooleanValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getBooleanValue().get())
                .findAny();
    }

    /**
     * Gets a String option by its name.
     *
     * @param name The name of the option.
     * @return The String value.
     */
    public Optional<String> getStringOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getStringValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getStringValue().get())
                .findAny();
    }

    /**
     * Gets a String option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The String value.
     */
    public Optional<String> getStringOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getStringValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getStringValue().get())
                .findAny();
    }

    /**
     * Gets a Integer option by its name.
     *
     * @param name The name of the option.
     * @return The integer value.
     */
    public Optional<Integer> getIntegerOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getLongValue().map(Long::intValue).isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getLongValue().map(Long::intValue).get())
                .findAny();
    }

    /**
     * Gets a Integer option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The integer value.
     */
    public Optional<Integer> getIntegerOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getLongValue().map(Long::intValue).isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getLongValue().map(Long::intValue).get())
                .findAny();
    }

    /**
     * Gets a ServerChannel option by its name.
     *
     * @param name The name of the option.
     * @return The ServerChannel value.
     */
    public Optional<ServerChannel> getChannelOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getChannelValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getChannelValue().get())
                .findAny();
    }

    /**
     * Gets a ServerChannel option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The ServerChannel value.
     */
    public Optional<ServerChannel> getChannelOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getChannelValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getChannelValue().get())
                .findAny();
    }

    /**
     * Gets a User option by its name.
     *
     * @param name The name of the option.
     * @return The User value.
     */
    public Optional<User> getUserOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getUserValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getUserValue().get())
                .findAny();
    }

    /**
     * Gets a User option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The User value.
     */
    public Optional<User> getUserOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getUserValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getUserValue().get())
                .findAny();
    }

    /**
     * Gets a Role option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The Role value.
     */
    public Optional<Role> getRoleOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getRoleValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getRoleValue().get())
                .findAny();
    }

    /**
     * Gets a Role option by its name.
     *
     * @param name The name of the option.
     * @return The Role value.
     */
    public Optional<Role> getRoleOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getRoleValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getRoleValue().get())
                .findAny();
    }

    /**
     * Gets a Mentionable option by its name, this is meant to be casted into
     * its own value. Make sure to do instanceof checks before trying to cast.
     *
     * @param name The name of the option.
     * @return The Mentionable value.
     */
    public Optional<Mentionable> getMentionableOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getMentionableValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getMentionableValue().get())
                .findAny();
    }

    /**
     * Gets a Mentionable option by its name, this is meant to be casted into
     * its own value. Make sure to do instanceof checks before trying to cast.
     * (ignore casing).
     *
     * @param name The name of the option.
     * @return The Mentionable value.
     */
    public Optional<Mentionable> getMentionableOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getMentionableValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getMentionableValue().get())
                .findAny();
    }


}
