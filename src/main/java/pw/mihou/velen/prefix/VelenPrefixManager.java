package pw.mihou.velen.prefix;

import pw.mihou.velen.prefix.loaders.PrefixLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a prefix manager that can be used to provide prefixes
 * to per-server or a single prefix for all servers, learn more at <a href="https://github.com/ShindouMihou/Velen/">Velen's README GitHub repository</a>
 */
public class VelenPrefixManager {

    private final Map<Long, String> prefixes = new ConcurrentHashMap<>();
    private final PrefixLoader prefixLoader;
    private final String defaultPrefix;

    /**
     * Creates a new prefix manager that supports per-server prefixes.
     * The per-server prefixes is done with the help of a user-provided {@link PrefixLoader} which
     * is used to provide the prefix of the server, you can run database methods on it, etc.
     * <p>
     * An example of a PrefixLoader can be found on
     * <a href="https://github.com/ShindouMihou/Velen/">Velen's README GitHub repository</a> where we
     * go in detail over how a Prefix Loader works.
     *
     * @param defaultPrefix The default prefix to use.
     * @param prefixLoader  The prefix loader.
     */
    public VelenPrefixManager(String defaultPrefix, PrefixLoader prefixLoader) {
        this.prefixLoader = prefixLoader;
        this.defaultPrefix = defaultPrefix;
    }

    /**
     * Creates a new prefix manager that supports only a single prefix.
     *
     * @param defaultPrefix The default prefix to use.
     */
    public VelenPrefixManager(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
        this.prefixLoader = null;
    }

    /**
     * Gets all the prefixes of the bot.
     *
     * @return All the server prefixes.
     */
    public Map<Long, String> getPrefixes() {
        return prefixes;
    }

    /**
     * Resets the prefix of the server to the default prefix.
     * This does not affect the prefix saved on your database, you will
     * have to program it yourself.
     *
     * @param server The server which the client will reset.
     */
    public void resetPrefix(long server) {
        prefixes.put(server, defaultPrefix);
    }

    /**
     * The default prefix that is being used.
     *
     * @return The default prefix of the bot.
     */
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    /**
     * Removes the stored prefix for the server which allows
     * reloading of the prefix.
     *
     * @param server The server to remove prefix from.
     */
    public void clearPrefix(long server) {
        prefixes.remove(server);
    }

    /**
     * Reloads the prefix of the server, this only works
     * if you are using a {@link PrefixLoader}.
     *
     * @param server The server to reload the prefix from.
     */
    public void reloadPrefix(long server) {
        if (prefixLoader != null) {
            String prefix = prefixLoader.load(server);

            if(prefix != null)
                prefixes.put(server, prefixLoader.load(server));

            if(prefix == null)
                prefixes.put(server, defaultPrefix);
        }
    }

    /**
     * Sets the prefix of the server, you can use this
     * to change the prefix of the server after changing it from
     * your database.
     *
     * @param server The server to change prefix.
     * @param prefix The prefix to set.
     */
    public void setPrefix(long server, String prefix) {
        prefixes.put(server, prefix);
    }

    /**
     * Gets the prefix of the server, will return to
     * default prefix if no {@link PrefixLoader} is set.
     *
     * @param server The server to retrieve the prefix of.
     * @return The prefix of the server.
     */
    public String getPrefix(long server) {
        if (prefixes.containsKey(server))
            return prefixes.get(server);

        if (prefixLoader == null)
            return defaultPrefix;

        String prefix = prefixLoader.load(server);
        prefix = prefix == null ? defaultPrefix : prefix;

        prefixes.put(server, prefix);
        return prefix;
    }

}
