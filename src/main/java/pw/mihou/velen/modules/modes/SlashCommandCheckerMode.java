package pw.mihou.velen.modules.modes;

public enum SlashCommandCheckerMode {

    /**
     * Normal mode means the checker should register any {@link pw.mihou.velen.interfaces.VelenCommand} that isn't
     * registered already while also updating the ones that have differing values, this is preferred way.
     */
    NORMAL,

    /**
     * Soft mode means the checker should not register any {@link pw.mihou.velen.interfaces.VelenCommand} that hasn't been
     * registered already but still update any slash commands that have different values from the {@link pw.mihou.velen.interfaces.VelenCommand}.
     */
    SOFT

}
