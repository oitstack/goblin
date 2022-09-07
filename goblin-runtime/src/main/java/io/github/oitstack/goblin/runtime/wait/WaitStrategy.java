package io.github.oitstack.goblin.runtime.wait;

import io.github.oitstack.goblin.runtime.Runtime;

import java.time.Duration;

public interface WaitStrategy {

    void waitUntilReady(Runtime runtime);

    WaitStrategy withStartupTimeout(Duration startupTimeout);
}