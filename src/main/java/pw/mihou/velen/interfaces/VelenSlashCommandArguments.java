package pw.mihou.velen.interfaces;

import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteractionOption;

import java.util.List;
import java.util.Optional;

public class VelenSlashCommandArguments implements VelenArguments {

    private final List<SlashCommandInteractionOption> options;

    /**
     * Creates a new Velen Slash Command Option wrapper.
     * This wraps the slash commands in a way that makes it easier to
     * look through without having to go through an ifPresent cluster.
     *
     * @param options The options.
     */
    public VelenSlashCommandArguments(List<SlashCommandInteractionOption> options) {
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

    @Override
    public Optional<Boolean> getBooleanOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getBooleanValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getBooleanValue().get())
                .findAny();
    }

    @Override
    public Optional<Boolean> getBooleanOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getBooleanValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getBooleanValue().get())
                .findAny();
    }

    @Override
    public Optional<String> getStringOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getStringValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getStringValue().get())
                .findAny();
    }

    @Override
    public Optional<String> getStringOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getStringValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getStringValue().get())
                .findAny();
    }

    @Override
    public Optional<Integer> getIntegerOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getIntValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getIntValue().get())
                .findAny();
    }

    @Override
    public Optional<Integer> getIntegerOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getIntValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getIntValue().get())
                .findAny();
    }

    @Override
    public Optional<ServerChannel> getChannelOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getChannelValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getChannelValue().get())
                .findAny();
    }

    @Override
    public Optional<ServerChannel> getChannelOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getChannelValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getChannelValue().get())
                .findAny();
    }

    @Override
    public Optional<User> getUserOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getUserValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getUserValue().get())
                .findAny();
    }

    @Override
    public Optional<User> getUserOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getUserValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getUserValue().get())
                .findAny();
    }

    @Override
    public Optional<Role> getRoleOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getRoleValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getRoleValue().get())
                .findAny();
    }

    @Override
    public Optional<Role> getRoleOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getRoleValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getRoleValue().get())
                .findAny();
    }

    @Override
    public Optional<Mentionable> getMentionableOptionWithName(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equals(name) && slashCommandOption.getMentionableValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getMentionableValue().get())
                .findAny();
    }

    @Override
    public Optional<Mentionable> getMentionableOptionWithNameIgnoreCasing(String name) {
        return options.stream().filter(slashCommandOption -> slashCommandOption.getName()
                .equalsIgnoreCase(name) && slashCommandOption.getMentionableValue().isPresent())
                .map(slashCommandInteractionOption -> slashCommandInteractionOption.getMentionableValue().get())
                .findAny();
    }

    @Override
    public Optional<Boolean> getBooleanOptionWithIndex(int index) {
        return index >= options.size() ? Optional.empty() : options.get(index).getBooleanValue();
    }

    @Override
    public Optional<String> getStringOptionWithIndex(int index) {
        return index >= options.size() ? Optional.empty() : options.get(index).getStringValue();
    }

    @Override
    public Optional<Integer> getIntegerOptionWithIndex(int index) {
        return index >= options.size() ? Optional.empty() : options.get(index).getIntValue();
    }

    @Override
    public Optional<ServerChannel> getChannelOptionWithIndex(int index) {
        return index >= options.size() ? Optional.empty() : options.get(index).getChannelValue();
    }

    @Override
    public Optional<User> getUserOptionWithIndex(int index) {
        return index >= options.size() ? Optional.empty() : options.get(index).getUserValue();
    }

    @Override
    public Optional<Role> getRoleOptionWithIndex(int index) {
        return index >= options.size() ? Optional.empty() : options.get(index).getRoleValue();
    }

    @Override
    public Optional<Mentionable> getMentionableOptionWithIndex(int index) {
        return index >= options.size() ? Optional.empty() : options.get(index).getMentionableValue();
    }


}
