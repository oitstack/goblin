package io.github.oitstack.goblin.runtime.health;

import io.github.oitstack.goblin.runtime.RuntimeState;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class ExternalPortListeningCheck implements Callable<Boolean> {
    private final RuntimeState runtimeState;
    private final Set<Integer> externalLivenessCheckPorts;

    @Override
    public Boolean call() {
        String address = runtimeState.getHost();

        externalLivenessCheckPorts.parallelStream().forEach(externalPort -> {
            try {
                new Socket(address, externalPort).close();
            } catch (IOException e) {
                throw new IllegalStateException("Socket not listening yet: " + externalPort);
            }
        });
        return true;
    }
}
