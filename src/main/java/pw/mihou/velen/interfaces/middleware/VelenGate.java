package pw.mihou.velen.interfaces.middleware;

import pw.mihou.velen.utils.Pair;

public class VelenGate {

    /**
     * Creates an acceptance response that allows a command from executing.
     *
     * @return A response that will be accepted by Velen.
     */
    public Pair<Boolean, String> allow() {
        return Pair.of(true, null);
    }

    /**
     * Creates a denial response that denies the command from executing.
     *
     * @param reason The reason why this execution was rejected.
     * @return A response that will be accepted by Velen.
     */
    public Pair<Boolean, String> deny(String reason) {
        return Pair.of(false, reason);
    }

    /**
     * Creates a denial response that denies the command from executing.
     *
     * @return A response that will be accepted by Velen.
     */
    public Pair<Boolean, String> deny() {
        return Pair.of(false, null);
    }

}
