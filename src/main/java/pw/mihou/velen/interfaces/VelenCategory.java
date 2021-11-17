package pw.mihou.velen.interfaces;

import pw.mihou.velen.interfaces.afterware.VelenAfterware;
import pw.mihou.velen.interfaces.afterware.types.VelenHybridAfterware;
import pw.mihou.velen.interfaces.afterware.types.VelenMessageAfterware;
import pw.mihou.velen.interfaces.afterware.types.VelenSlashAfterware;
import pw.mihou.velen.interfaces.middleware.VelenMiddleware;
import pw.mihou.velen.interfaces.middleware.types.VelenHybridMiddleware;
import pw.mihou.velen.interfaces.middleware.types.VelenMessageMiddleware;
import pw.mihou.velen.interfaces.middleware.types.VelenSlashMiddleware;

import java.util.List;
import java.util.stream.Collectors;

public interface VelenCategory {

    /**
     * Retrieves all the middlewares that are globally attached to every
     * command in this category.
     *
     * @return The middlewares used in this category.
     */
    List<VelenMiddleware> getMiddlewares();

    /**
     * Retrieves the hybrid command middlewares that are globally attached
     * to every command in this category.
     *
     * @return The middlewares used in this category.
     */
    default List<VelenHybridMiddleware> getHybridMiddlewares() {
        return getMiddlewares()
                .stream()
                .filter(middleware -> middleware instanceof VelenHybridMiddleware)
                .map(middleware -> (VelenHybridMiddleware) middleware)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the message command middlewares that are globally attached
     * to every command in this category.
     *
     * @return The middlewares used in this category.
     */
    default List<VelenMessageMiddleware> getMessageMiddlewares() {
        return getMiddlewares()
                .stream()
                .filter(middleware -> middleware instanceof VelenMessageMiddleware)
                .map(middleware -> (VelenMessageMiddleware) middleware)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the slash command middlewares that are globally attached
     * to every command in this category.
     *
     * @return The middlewares used in this category.
     */
    default List<VelenSlashMiddleware> getSlashMiddlewares() {
        return getMiddlewares()
                .stream()
                .filter(middleware -> middleware instanceof VelenSlashMiddleware)
                .map(middleware -> (VelenSlashMiddleware) middleware)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all the afterwares that are globally attached to every
     * command in this category.
     *
     * @return The afterwares used in this category.
     */
    List<VelenAfterware> getAfterwares();

    /**
     * Retrieves the hybrid command afterwares that are globally attached
     * to every command in this category.
     *
     * @return The afterwares used in this category.
     */
    default List<VelenHybridAfterware> getHybridAfterwares() {
        return getAfterwares()
                .stream()
                .filter(afterware -> afterware instanceof VelenHybridAfterware)
                .map(afterware -> (VelenHybridAfterware) afterware)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the message command afterwares that are globally attached
     * to every command in this category.
     *
     * @return The afterwares used in this category.
     */
    default List<VelenMessageAfterware> getMessageAfterwares() {
        return getAfterwares()
                .stream()
                .filter(afterware -> afterware instanceof VelenMessageAfterware)
                .map(afterware -> (VelenMessageAfterware) afterware)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the slash command afterwares that are globally attached
     * to every command in this category.
     *
     * @return The afterwares used in this category.
     */
    default List<VelenSlashAfterware> getSlashAfterwares() {
        return getAfterwares()
                .stream()
                .filter(afterware -> afterware instanceof VelenSlashAfterware)
                .map(afterware -> (VelenSlashAfterware) afterware)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all the commands that are under this category.
     *
     * @return The list of commands under this category.
     */
    List<VelenCommand> getCommands();

    /**
     * Retrieves the name of this category.
     *
     * @return The name of this category.
     */
    String getName();

    /**
     * Retrieves the description of this category.
     *
     * @return The description of this category.
     */
    String getDescription();

}
