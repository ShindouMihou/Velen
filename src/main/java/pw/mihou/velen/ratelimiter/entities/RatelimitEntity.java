package pw.mihou.velen.ratelimiter.entities;

import pw.mihou.velen.utils.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RatelimitEntity {

    private final long user;
    private final Map<Long, Pair<AtomicBoolean, Long>> servers;

    public RatelimitEntity(long user) {
        this.user = user;
        this.servers = new ConcurrentHashMap<>();
    }

    public long getUser() {
        return this.user;
    }

    public void ratelimit(long server) {
        this.servers.put(server, Pair.of(new AtomicBoolean(false), System.currentTimeMillis()));
    }

    public long getRemainingTime(long server) {
        return this.servers.containsKey(server) ? this.servers.get(server).getRight() : 0L;
    }

    public boolean isNotified(long server) {
        return this.servers.containsKey(server) && this.servers.get(server).getLeft().get();
    }

    public void notified(long server) {
        if (isRatelimited(server)) {
            this.servers.get(server).getLeft().set(true);
        }
    }

    public boolean isRatelimited(long server) {
        return this.servers.containsKey(server);
    }

    public void release(long server) {
        this.servers.remove(server);
    }

}
