package pw.mihou.velen.interfaces.afterware;

import pw.mihou.velen.interfaces.afterware.types.VelenHybridAfterware;
import pw.mihou.velen.interfaces.afterware.types.VelenMessageAfterware;
import pw.mihou.velen.interfaces.afterware.types.VelenSlashAfterware;

/**
 * The parent interface for all the afterwares of Velen. You
 * are not supposed to use this but instead use {@link pw.mihou.velen.interfaces.afterware.types.VelenHybridAfterware} and
 * others.
 */
public interface VelenAfterware {

    /**
     * Creates a new afterware used for hybrid commands.
     *
     * @param afterware The afterware to use for hybrid commands.
     * @return A new hybrid command afterware.
     */
    static VelenHybridAfterware ofHybrid(VelenHybridAfterware afterware) {
        return afterware;
    }

    /**
     * Creates a new afterware used for message commands.
     *
     * @param afterware The afterware to use for message commands.
     * @return A new message command afterware.
     */
    static VelenMessageAfterware ofMessage(VelenMessageAfterware afterware) {
        return afterware;
    }

    /**
     * Creates a new afterware used for slash commands.
     *
     * @param afterware The afterware to use for slash commands.
     * @return A new slash command afterware.
     */
    static VelenSlashAfterware ofSlash(VelenSlashAfterware afterware) {
        return afterware;
    }

}
