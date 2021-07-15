package pw.mihou.velen.ratelimiter.entities;

public enum RatelimitInterceptorPosition {

    /**
     * This is used when you want to intercept
     * the rate-limiter when the user is about to be notified
     * to be rate-limited (this will only happen once every rate-limit
     * cycle and is used to indicate a user is rate-limited).
     */
    NOTIFICATION,

    /**
     * This is used when you want to intercept the
     * rate-limiter when the user is about to released from the cooldown.
     */
    RELEASE,

    /**
     * This is used when you want to intercept the rate-limiter
     * when the user has successfully passed the rate-limiter and the command
     * is about to be executed.
     */
    EXECUTION;

}
