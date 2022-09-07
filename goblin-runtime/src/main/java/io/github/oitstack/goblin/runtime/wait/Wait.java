package io.github.oitstack.goblin.runtime.wait;

import io.github.oitstack.goblin.runtime.docker.wait.strategy.DockerLogMessageWaitStrategy;

public class Wait {
    public static WaitStrategy defaultWaitStrategy() {
        return forListeningPort();
    }

    public static io.github.oitstack.goblin.runtime.wait.HostPortWaitStrategy forListeningPort() {
        return new HostPortWaitStrategy();
    }

    public static io.github.oitstack.goblin.runtime.wait.HttpWaitStrategy forHttp(String path) {
        return new io.github.oitstack.goblin.runtime.wait.HttpWaitStrategy()
                .forPath(path);
    }

    public static HttpWaitStrategy forHttps(String path) {
        return forHttp(path)
                .usingTls();
    }

    public static io.github.oitstack.goblin.runtime.wait.RuntimeHealthcheckWaitStrategy forHealthcheck() {
        return new RuntimeHealthcheckWaitStrategy();
    }
}
