package pw.mihou.velen.internals.observer.modes;

public enum ObserverMode {

    /**
     * This mode tells {@link pw.mihou.velen.internals.observer.VelenObserver} to only send a log
     * for every commands that needs to be updated or created, it will not do anything else.
     */
    WATCHDOG(false, false),

    /**
     * This mode tells {@link pw.mihou.velen.internals.observer.VelenObserver} to only update
     * the commands that needs to be updated to the Discord API, it will not create any new
     * commands.
     */
    UPDATE(true, false),

    /**
     * This mode tells {@link pw.mihou.velen.internals.observer.VelenObserver} to only register
     * the commands that needs to be registered to the Discord API, it will not update existing
     * commands.
     */
    CREATE(false, true),

    /**
     * This mode tells {@link pw.mihou.velen.internals.observer.VelenObserver} that it should
     * update or create any commands that needs to be created or updated.
     */
    MASTER(true, true);

    private final boolean update;
    private final boolean create;

    /**
     * Creates a new Observer Mode that either allows creation or
     * updating of commands.
     *
     * @param update To allow updating of commands?
     * @param create To allow creating of commands?
     */
    ObserverMode(boolean update, boolean create) {
        this.update = update;
        this.create = create;
    }

    /**
     * Does this observer mode allow updating of commands?
     *
     * @return {@link Boolean}
     */
    public boolean isUpdate() {
        return update;
    }

    /**
     * Does this observer mode allow creating of commands?
     *
     * @return {@link Boolean}
     */
    public boolean isCreate() {
        return create;
    }
}
