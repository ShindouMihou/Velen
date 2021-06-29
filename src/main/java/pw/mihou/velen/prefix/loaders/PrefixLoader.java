package pw.mihou.velen.prefix.loaders;

public interface PrefixLoader {

    /**
     * Computes or retrieves the prefix that corresponds
     * with the server id.
     *
     * @param key The server ID.
     * @return The prefix of the server.
     */
    String load(long key);

}
