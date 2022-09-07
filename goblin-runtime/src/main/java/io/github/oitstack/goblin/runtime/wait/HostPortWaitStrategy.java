package io.github.oitstack.goblin.runtime.wait;

import io.github.oitstack.goblin.runtime.exceptions.TimeoutException;
import io.github.oitstack.goblin.runtime.health.ExternalPortListeningCheck;
import io.github.oitstack.goblin.runtime.health.LinuxInternalCommandPortListeningCheck;
import io.github.oitstack.goblin.runtime.utils.RetryUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class HostPortWaitStrategy extends AbstractWaitStrategy {

  @Override
  protected void waitUntilReady() {
    final Set<Integer> externalLivenessCheckPorts = getLivenessCheckPorts();
    if (externalLivenessCheckPorts.isEmpty()) {
      if (log.isDebugEnabled()) {
        log.debug("Liveness check ports of {} is empty. Not waiting.", runtime.getImageName());
      }
      return;
    }

    @SuppressWarnings("unchecked")
    List<Integer> exposedPorts = runtime.getExposedPorts();

    final Set<Integer> internalPorts = getInternalPorts(externalLivenessCheckPorts, exposedPorts);

    Callable<Boolean> internalCheck =
        new LinuxInternalCommandPortListeningCheck(runtime, internalPorts);

    Callable<Boolean> externalCheck =
        new ExternalPortListeningCheck(runtime, externalLivenessCheckPorts);

    try {
      RetryUtils.retryUntilTrue(
          (int) startupTimeout.getSeconds(),
          TimeUnit.SECONDS,
          () -> internalCheck.call() && externalCheck.call());

    } catch (TimeoutException e) {
      throw new TimeoutException(
          "Timed out waiting for container port to open ("
              + runtime.getHost()
              + " ports: "
              + externalLivenessCheckPorts
              + " should be listening)");
    }
  }

  private Set<Integer> getInternalPorts(
      Set<Integer> externalLivenessCheckPorts, List<Integer> exposedPorts) {
    return exposedPorts.stream()
        .filter(it -> externalLivenessCheckPorts.contains(runtime.getPortByInnerPort(it)))
        .collect(Collectors.toSet());
  }
}
