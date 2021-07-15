package pw.mihou.velen.ratelimiter;

import pw.mihou.velen.ratelimiter.entities.RatelimitEntity;
import pw.mihou.velen.ratelimiter.entities.RatelimitInterceptorPosition;
import pw.mihou.velen.ratelimiter.entities.RatelimitObject;
import pw.mihou.velen.utils.Pair;
import pw.mihou.velen.utils.VelenThreadPool;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class VelenRatelimiter {

    private final Map<Pair<String, Long>, RatelimitEntity> ratelimits = new ConcurrentHashMap<>();
    private final Duration duration;
    private final List<Consumer<RatelimitObject>> interceptorRelease = new ArrayList<>();
    private final List<Consumer<RatelimitObject>> interceptorExecution = new ArrayList<>();
    private final List<Consumer<RatelimitObject>> interceptorNotification = new ArrayList<>();

    /**
     * Creates a Velen Rate-limiter with a custom default duration.
     *
     * @param duration The default duration of the cooldown.
     */
    public VelenRatelimiter(Duration duration) {
        this.duration = duration;
    }

    /**
     * Creates a Velen Rate-limiter with a default duration of 7 seconds.
     */
    public VelenRatelimiter() {
        this(Duration.of(5, ChronoUnit.SECONDS));
    }

    /**
     * Ratelimits a user from a command with the default duration.
     *
     * @param user      The user to rate-limit.
     * @param server    The server where the user will be rate-limited on the command.
     * @param command   The command which the user is rate-limited on.
     * @param onLimited This is executed when the user is rate-limited (once).
     *                  For example, if a user has executed a command three times and was rate-limited on the first execution.
     *                  This will be executed on the second time the user ran the command and on the third time, if the user
     *                  is still rate-limited, this will no longer execute. (In a sense, it's a one-time execution for the
     *                  entirety of the user's rate-limit duration).
     * @param onSuccess This is executed when the user is not rate-limited and is usually where you throw in
     *                  the execution of the command.
     */
    public void ratelimit(long user, long server, String command, Consumer<Long> onLimited, Consumer<RatelimitEntity> onSuccess) {
        ratelimit(user, server, command, duration.toMillis(), onLimited, onSuccess);
    }

    /**
     * Adds an interceptor to the rate-limiter event, you can use this
     * to log specific events like when the user is about to be rate-limited,
     * when the user is released from the rate-limit or when the user
     * has used the command successfully.
     * <p>
     * All of these events are ran asynchronously to not affect with the operations
     * of the normal commands.
     *
     * @param event    The interceptor event to trigger.
     * @param position The position where the event should be triggered.
     */
    public void addInterceptor(Consumer<RatelimitObject> event, RatelimitInterceptorPosition position) {
        if (position == RatelimitInterceptorPosition.EXECUTION)
            interceptorExecution.add(event);

        if (position == RatelimitInterceptorPosition.NOTIFICATION)
            interceptorNotification.add(event);

        if (position == RatelimitInterceptorPosition.RELEASE)
            interceptorRelease.add(event);
    }

    /**
     * Ratelimits a user from a command.
     *
     * @param user      The user to rate-limit.
     * @param server    The server where the user will be rate-limited on the command.
     * @param command   The command which the user is rate-limited on.
     * @param cooldown  The amount of time in milliseconds to use.
     * @param onLimited This is executed when the user is rate-limited (once).
     *                  For example, if a user has executed a command three times and was rate-limited on the first execution.
     *                  This will be executed on the second time the user ran the command and on the third time, if the user
     *                  is still rate-limited, this will no longer execute. (In a sense, it's a one-time execution for the
     *                  entirety of the user's rate-limit duration).
     * @param onSuccess This is executed when the user is not rate-limited and is usually where you throw in
     *                  the execution of the command.
     */
    public void ratelimit(long user, long server, String command, long cooldown, Consumer<Long> onLimited, Consumer<RatelimitEntity> onSuccess) {
        Pair<String, Long> pair = Pair.of(command, user);
        if (!ratelimits.containsKey(pair)) {
            ratelimits.put(pair, new RatelimitEntity(user));
        }

        RatelimitObject ratelimitObject = new RatelimitObject(user, server, command);

        RatelimitEntity entity = ratelimits.get(pair);
        if (entity.isRatelimited(server)) {
            if (((TimeUnit.MILLISECONDS.toSeconds(entity.getRemainingTime(server) + cooldown))
                    - (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))) > 0) {
                if (!entity.isNotified(server)) {
                    onLimited.accept((TimeUnit.MILLISECONDS.toSeconds(entity.getRemainingTime(server) + cooldown))
                            - (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));

                    if (!interceptorNotification.isEmpty())
                        interceptorNotification.forEach(ratelimitObjectConsumer ->
                                VelenThreadPool.executorService.submit(() -> ratelimitObjectConsumer.accept(ratelimitObject)));

                    entity.notified(server);
                }

                return;
            }

            entity.release(server);
        }

        entity.ratelimit(server);

        if (!interceptorExecution.isEmpty())
            interceptorExecution.forEach(ratelimitObjectConsumer ->
                    VelenThreadPool.executorService.submit(() -> ratelimitObjectConsumer.accept(ratelimitObject)));
        onSuccess.accept(entity);
    }

    /**
     * Releases a user from being rate-limited.
     *
     * @param user    The user to release.
     * @param server  The server where the user is from.
     * @param command The command that the user used.
     */
    public void release(long user, long server, String command) {
        Pair<String, Long> pair = Pair.of(command, user);
        if (!ratelimits.containsKey(pair))
            ratelimits.put(pair, new RatelimitEntity(user));

        if (!interceptorRelease.isEmpty())
            interceptorExecution.forEach(ratelimitObjectConsumer ->
                    VelenThreadPool.executorService.submit(() -> ratelimitObjectConsumer
                            .accept(new RatelimitObject(user, server, command))));

        ratelimits.get(pair).release(server);
    }

    /**
     * This is used to check if the user is rate-limited or not.
     *
     * @param user    The user to check.
     * @param server  The server where the user is from.
     * @param command The command that the user used.
     * @return Is the user rate-limited?
     */
    public boolean isRatelimited(long user, long server, String command) {
        Pair<String, Long> pair = Pair.of(command, user);
        if (!ratelimits.containsKey(pair)) {
            ratelimits.put(pair, new RatelimitEntity(user));
            return false;
        }

        return ratelimits.get(pair).isRatelimited(server);
    }

    /**
     * Returns a full map containing a key of (Command Name, Server ID) and a value of
     * the user's rate-limit entity.
     *
     * @return A map containing a key of (Command Name, Server ID) and a value of
     * the user's rate-limit entity.
     */
    public Map<Pair<String, Long>, RatelimitEntity> getRatelimitedUsers() {
        return ratelimits;
    }

    /**
     * The default duration that Velen will use if the command specified does not have
     * a duration specified.
     *
     * @return The default duration.
     */
    public Duration getDefaultCooldown() {
        return duration;
    }

}
