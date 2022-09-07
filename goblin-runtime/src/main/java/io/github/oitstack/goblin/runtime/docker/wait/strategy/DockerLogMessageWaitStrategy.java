package io.github.oitstack.goblin.runtime.docker.wait.strategy;

import com.github.dockerjava.api.command.LogContainerCmd;
import io.github.oitstack.goblin.runtime.docker.container.DockerContainerAdapter;
import io.github.oitstack.goblin.runtime.docker.output.ExecResultCallback;
import io.github.oitstack.goblin.runtime.docker.output.OutputFrame;
import io.github.oitstack.goblin.runtime.docker.output.WaitingListener;
import io.github.oitstack.goblin.runtime.exceptions.RuntimeLaunchException;
import io.github.oitstack.goblin.runtime.wait.AbstractWaitStrategy;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class DockerLogMessageWaitStrategy extends AbstractWaitStrategy {

    private String regEx;

    private int times = 1;

    @Override
    @SneakyThrows(IOException.class)
    protected void waitUntilReady() {
        DockerContainerAdapter dockerRuntime=(DockerContainerAdapter)runtime;

        WaitingListener waitingListener = new WaitingListener();
        LogContainerCmd cmd = dockerRuntime.getGoblinDockerClient().logContainerCmd(runtime.getRuntimeId())
                .withFollowStream(true)
                .withSince(0)
                .withStdOut(true)
                .withStdErr(true);

        try (ExecResultCallback callback = new ExecResultCallback()) {
            callback.register(OutputFrame.OutputType.STDOUT, waitingListener);
            callback.register(OutputFrame.OutputType.STDERR, waitingListener);

            cmd.exec(callback);

            Predicate<OutputFrame> waitPredicate = outputFrame ->
                    // (?s) enables line terminator matching (equivalent to Pattern.DOTALL)
                    outputFrame.getUtf8String().matches("(?s)" + regEx);

            try {
                waitingListener.waitUntil(waitPredicate, startupTimeout.getSeconds(), TimeUnit.SECONDS, times);
            } catch (TimeoutException e) {
                throw new RuntimeLaunchException("Timed out waiting for log output matching '" + regEx + "'");
            }
        }
    }

    public DockerLogMessageWaitStrategy withRegEx(String regEx) {
        this.regEx = regEx;
        return this;
    }

    public DockerLogMessageWaitStrategy withTimes(int times) {
        this.times = times;
        return this;
    }
}
