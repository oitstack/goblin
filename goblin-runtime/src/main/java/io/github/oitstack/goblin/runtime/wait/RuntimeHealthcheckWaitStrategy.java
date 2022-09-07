package io.github.oitstack.goblin.runtime.wait;

import io.github.oitstack.goblin.runtime.exceptions.TimeoutException;
import io.github.oitstack.goblin.runtime.utils.RetryUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RuntimeHealthcheckWaitStrategy extends AbstractWaitStrategy {

  @Override
  protected void waitUntilReady() {

    try {
      RetryUtils.retryUntilTrue(startupTimeout.getSeconds(), TimeUnit.SECONDS, runtime::isRunning);
    } catch (TimeoutException e) {
      log.error("RuntimeHealthcheckWaitStrategy wait timeout!",e);
    }
  }
}
