package pw.mihou.velen.interfaces;

import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import java.util.Optional;

public interface VelenArguments {

    /**
     * Gets a boolean option by its name.
     *
     * @param name The name of the option.
     * @return The boolean value.
     */
    Optional<Boolean> getBooleanOptionWithName(String name);

    /**
     * Gets a boolean option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The boolean value.
     */
    Optional<Boolean> getBooleanOptionWithNameIgnoreCasing(String name);

    /**
     * Gets a String option by its name.
     *
     * @param name The name of the option.
     * @return The String value.
     */
    Optional<String> getStringOptionWithName(String name);

    /**
     * Gets a String option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The String value.
     */
    Optional<String> getStringOptionWithNameIgnoreCasing(String name);

    /**
     * Gets a Integer option by its name.
     *
     * @param name The name of the option.
     * @return The integer value.
     */
    Optional<Integer> getIntegerOptionWithName(String name);

    /**
     * Gets a Integer option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The integer value.
     */
    Optional<Integer> getIntegerOptionWithNameIgnoreCasing(String name);

    /**
     * Gets a ServerChannel option by its name.
     *
     * @param name The name of the option.
     * @return The ServerChannel value.
     */
    Optional<ServerChannel> getChannelOptionWithName(String name);

    /**
     * Gets a ServerChannel option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The ServerChannel value.
     */
    Optional<ServerChannel> getChannelOptionWithNameIgnoreCasing(String name);

    /**
     * Gets a User option by its name.
     *
     * @param name The name of the option.
     * @return The User value.
     */
    Optional<User> getUserOptionWithName(String name);

    /**
     * Gets a User option by its name (ignore casing).
     *
     * @param name The name of the option.
     * @return The User value.
     */
    Optional<User> getUserOptionWithNameIgnoreCasing(String name);

    /**
     * Gets a Role option by its name.
     *
     * @param name The name of the option.
     * @return The Role value.
     */
    Optional<Role> getRoleOptionWithName(String name);

    /**
     * Gets a Role option by its name. (ignore casing)
     *
     * @param name The name of the option.
     * @return The Role value.
     */
    Optional<Role> getRoleOptionWithNameIgnoreCasing(String name);

    /**
     * Gets a Mentionable option by its name, this is meant to be casted into
     * its own value. Make sure to do instanceof checks before trying to cast.
     *
     * @param name The name of the option.
     * @return The Mentionable value.
     */
    Optional<Mentionable> getMentionableOptionWithName(String name);

    /**
     * Gets a Mentionable option by its name, this is meant to be casted into
     * its own value. Make sure to do instanceof checks before trying to cast.
     * (ignore casing).
     *
     * @param name The name of the option.
     * @return The Mentionable value.
     */
    public Optional<Mentionable> getMentionableOptionWithNameIgnoreCasing(String name);

    /**
     * Gets a boolean option by its index.
     *
     * @param index The index of the option.
     * @return The boolean value.
     */
    Optional<Boolean> getBooleanOptionWithIndex(int index);

    /**
     * Gets a String option by its index.
     *
     * @param index The index of the option.
     * @return The String value.
     */
    Optional<String> getStringOptionWithIndex(int index);

    /**
     * Gets a Integer option by its index.
     *
     * @param index The name of the option.
     * @return The integer value.
     */
    Optional<Integer> getIntegerOptionWithIndex(int index);

    /**
     * Gets a ServerChannel option by its index.
     *
     * @param index The index of the option.
     * @return The ServerChannel value.
     */
    Optional<ServerChannel> getChannelOptionWithIndex(int index);

    /**
     * Gets a User option by its index.
     *
     * @param index The index of the option.
     * @return The User value.
     */
    Optional<User> getUserOptionWithIndex(int index);

    /**
     * Gets a Role option by its index.
     *
     * @param index The index of the option.
     * @return The Role value.
     */
    Optional<Role> getRoleOptionWithIndex(int index);

    /**
     * Gets a Mentionable option by its index, this is meant to be casted into
     * its own value. Make sure to do instanceof checks before trying to cast.
     *
     * @param index The index of the option.
     * @return The Mentionable value.
     */
    Optional<Mentionable> getMentionableOptionWithIndex(int index);

}