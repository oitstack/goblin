package io.github.oitstack.goblin.runtime.wait;

import com.google.common.util.concurrent.RateLimiter;
import io.github.oitstack.goblin.runtime.Runtime;

import java.time.Duration;
import java.util.Set;

public abstract class AbstractWaitStrategy implements WaitStrategy {


    private static final RateLimiter DOCKER_CLIENT_RATE_LIMITER =RateLimiter.create(1);

    protected Runtime runtime;

    protected Duration startupTimeout = Duration.ofSeconds(60);

    private RateLimiter rateLimiter = DOCKER_CLIENT_RATE_LIMITER;


    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public void setRateLimiter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void waitUntilReady(Runtime runtime) {
        this.runtime = runtime;
        waitUntilReady();
    }

    protected abstract void waitUntilReady();

    public WaitStrategy withStartupTimeout(Duration startupTimeout) {
        this.startupTimeout = startupTimeout;
        return this;
    }

    protected Set<Integer> getLivenessCheckPorts() {
        return runtime.getLivenessCheckPortNumbers();
    }

}