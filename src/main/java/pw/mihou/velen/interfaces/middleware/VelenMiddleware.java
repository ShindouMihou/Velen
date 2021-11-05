package pw.mihou.velen.interfaces.middleware;

import pw.mihou.velen.interfaces.middleware.types.VelenHybridMiddleware;
import pw.mihou.velen.interfaces.middleware.types.VelenMessageMiddleware;
import pw.mihou.velen.interfaces.middleware.types.VelenSlashMiddleware;

/**
 * The parent interface for all the middlewares of Velen. You
 * are not supposed to use this but instead use {@link pw.mihou.velen.interfaces.middleware.types.VelenHybridMiddleware} and
 * others.
 */
public interface VelenMiddleware {

    /**
     * Creates a new middleware used for hybrid commands.
     *
     * @param middleware The middleware to use for hybrid commands.
     * @return A new hybrid command middleware.
     */
    static VelenHybridMiddleware ofHybrid(VelenHybridMiddleware middleware) {
        return middleware;
    }

    /**
     * Creates a new middleware used for message commands.
     *
     * @param middleware The middleware to use for message commands.
     * @return A new message command middleware.
     */
    static VelenMessageMiddleware ofMessage(VelenMessageMiddleware middleware) {
        return middleware;
    }

    /**
     * Creates a new middleware used for slash commands.
     *
     * @param middleware The middleware to use for slash commands.
     * @return A new slash command middleware.
     */
    static VelenSlashMiddleware ofSlash(VelenSlashMiddleware middleware) {
        return middleware;
    }


}
