package io.github.oitstack.goblin.runtime.health;

import io.github.oitstack.goblin.runtime.Runtime;
import io.github.oitstack.goblin.runtime.RuntimeOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
public abstract class InternalCommandPortListeningCheck implements java.util.concurrent.Callable<Boolean> {

    private  io.github.oitstack.goblin.runtime.Runtime runtime;
    private  Set<Integer> internalPorts;

    public Runtime getRuntime() {
        return runtime;
    }

    public void setRuntime(Runtime runtime) {
        this.runtime = runtime;
    }

    public Set<Integer> getInternalPorts() {
        return internalPorts;
    }

    public void setInternalPorts(Set<Integer> internalPorts) {
        this.internalPorts = internalPorts;
    }

    public InternalCommandPortListeningCheck(Runtime runtime, Set<Integer> internalPorts){
        this.runtime=runtime;
        this.internalPorts=internalPorts;
    }
    @Override
    public Boolean call() {
        String command = generateCheckPortCmd();

        Instant before = Instant.now();
        try {
            RuntimeOperation.ExecResult result=runtime.execInRuntime("/bin/sh", "-c", command.toString());
            log.trace("Check for {} took {}. Result code '{}', stdout message: '{}'", internalPorts, Duration.between(before, Instant.now()), result.getExitCode(), result.getStdout());
            return result.getExitCode() == "0";
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected abstract String generateCheckPortCmd();
}
