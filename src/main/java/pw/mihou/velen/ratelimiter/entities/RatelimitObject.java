package pw.mihou.velen.ratelimiter.entities;

import java.util.Objects;

public class RatelimitObject {

    private final long user;
    private final long server;
    private final String command;

    /**
     * Creates a new rate-limit object which is used
     * to handle interceptors for rate-limit.
     *
     * @param user    The user who was rate-limited.
     * @param server  The server where the user was rate-limited.
     * @param command The command in which the user was rate-limited.
     */
    public RatelimitObject(long user, long server, String command) {
        this.user = user;
        this.server = server;
        this.command = command;
    }

    /**
     * Retrieves the ID of the user who was
     * rate-limited on the specific command and server.
     *
     * @return The user who was rate-limited.
     */
    public long getUser() {
        return user;
    }

    /**
     * Retrieves the ID of the server where the
     * user was rate-limited.
     *
     * @return The server ID.
     */
    public long getServer() {
        return server;
    }

    /**
     * Retrieves the name of the command and
     * the cooldown, this utilizes the toString method
     * of the command deep inside.
     *
     * @return The command name.
     */
    public String getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatelimitObject that = (RatelimitObject) o;
        return getUser() == that.getUser() &&
                getServer() == that.getServer() &&
                Objects.equals(getCommand(), that.getCommand());
    }

    @Override
    public String toString() {
        return "Ratelimit Object (User " + user + ", Server " + server + ", Command [" + command + "])";
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getServer(), getCommand());
    }
}
