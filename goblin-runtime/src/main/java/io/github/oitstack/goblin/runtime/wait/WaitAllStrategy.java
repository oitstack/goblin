package io.github.oitstack.goblin.runtime.wait;

import io.github.oitstack.goblin.runtime.Runtime;
import io.github.oitstack.goblin.runtime.utils.Timeouts;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WaitAllStrategy implements WaitStrategy {

    public enum Mode {

        WITH_OUTER_TIMEOUT,

        WITH_INDIVIDUAL_TIMEOUTS_ONLY,

        WITH_MAXIMUM_OUTER_TIMEOUT
    }

    private final Mode mode;
    private final List<WaitStrategy> strategies = new ArrayList<>();
    private Duration timeout = Duration.ofSeconds(30);

    public WaitAllStrategy() {
        this(Mode.WITH_OUTER_TIMEOUT);
    }

    public WaitAllStrategy(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void waitUntilReady(Runtime runtime) {
        if (mode == Mode.WITH_INDIVIDUAL_TIMEOUTS_ONLY) {
            waitUntilNestedStrategiesAreReady(runtime);
        } else {
            Timeouts.doWithTimeout((int) timeout.toMillis(), TimeUnit.MILLISECONDS, () -> {
                waitUntilNestedStrategiesAreReady(runtime);
            });
        }
    }

    private void waitUntilNestedStrategiesAreReady(Runtime runtime) {
        for (WaitStrategy strategy : strategies) {
            strategy.waitUntilReady(runtime);
        }
    }

    public WaitAllStrategy withStrategy(WaitStrategy strategy) {

        if (mode == Mode.WITH_OUTER_TIMEOUT) {
            applyStartupTimeout(strategy);
        }

        this.strategies.add(strategy);
        return this;
    }

    @Override
    public WaitAllStrategy withStartupTimeout(Duration startupTimeout) {

        if (mode == Mode.WITH_INDIVIDUAL_TIMEOUTS_ONLY) {
            throw new IllegalStateException(String.format(
                    "Changing startup timeout is not supported with mode %s", Mode.WITH_INDIVIDUAL_TIMEOUTS_ONLY));
        }

        this.timeout = startupTimeout;
        strategies.forEach(this::applyStartupTimeout);
        return this;
    }

    private void applyStartupTimeout(WaitStrategy childStrategy) {
        childStrategy.withStartupTimeout(this.timeout);
    }
}

