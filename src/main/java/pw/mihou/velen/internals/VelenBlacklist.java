package pw.mihou.velen.internals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class VelenBlacklist {

    private final List<Long> blacklist;
    private Function<Long, Boolean> loader;

    /**
     * This creates an empty, ordinary {@link VelenBlacklist} that
     * is not persistent.
     */
    public VelenBlacklist() {
        this.blacklist = new ArrayList<>();
    }

    /**
     * This creates an {@link VelenBlacklist} that takes
     * in the data from the blacklist parameter to utilize
     * as a list of blacklisted users.
     *
     * @param blacklist The blacklisted users.
     */
    public VelenBlacklist(List<Long> blacklist) {
        this.blacklist = blacklist;
    }

    /**
     * Creates an {@link VelenBlacklist} that uses a loader to load in
     * the blacklisted users from a persistent database or a database in general.
     *
     * @param loader This is used by {@link pw.mihou.velen.internals.VelenBlacklist} to check if
     *               a user is blacklisted or not (this should be used if you are running a persistent blacklist
     *               that utilizes a database like MongoDB or Redis).
     *               <p>
     *               The method requires you to return a boolean because the library will check if
     *               the user is blacklisted through the state of the boolean <b>(true = blacklisted; false = not blacklisted)</b>.
     */
    public VelenBlacklist(Function<Long, Boolean> loader) {
        this.loader = loader;
        this.blacklist = new ArrayList<>();
    }

    /**
     * Checks if a user is blacklisted from using
     * any commands of the bot.
     *
     * @param user The user to check.
     * @return is this user blacklisted?
     */
    public boolean isBlacklisted(long user) {
        if (blacklist.contains(user))
            return true;

        if (loader != null) {
            boolean t = loader.apply(user);
            if (t)
                blacklist.add(user);

            return t;
        }

        return false;
    }

    /**
     * Refreshes the blacklist to check if the user
     * is still blacklisted or not, if the user was blacklisted
     * previously and is no longer blacklisted, then it will remove
     * from the list, otherwise, it will add.
     *
     * @param user The user to refresh.
     */
    public void refresh(long user) {
        if (loader != null) {
            if (loader.apply(user))
                blacklist.add(user);
            else
                blacklist.remove(user);
        }
    }

    /**
     * Removes the user from the internal blacklist if
     * they are listed on it.
     *
     * @param user The user to remove.
     */
    public void remove(long user) {
        blacklist.remove(user);
    }

    /**
     * Adds the user to the internal blacklist if
     * they are not yet listed on it.
     *
     * @param user The user to add.
     */
    public void add(long user) {
        blacklist.add(user);
    }

}
