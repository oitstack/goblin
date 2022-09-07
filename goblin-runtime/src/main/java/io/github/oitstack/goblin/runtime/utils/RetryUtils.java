package io.github.oitstack.goblin.runtime.utils;

import io.github.oitstack.goblin.runtime.exceptions.TimeoutException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RetryUtils {
  public static void retryUntilSuccess(long time, TimeUnit timeUnit, Runnable runnable)
      throws TimeoutException {
    long startTime = System.currentTimeMillis();
    long duration = timeUnit.toMillis(time);
    long remainTime = duration;
    Exception contextException = null;
    while (remainTime > 0) {
      try {
        Timeouts.doWithTimeout(remainTime, TimeUnit.MILLISECONDS, runnable);
      } catch (Exception e) {
        contextException = e;
      } finally {
        remainTime = duration - System.currentTimeMillis() - startTime;
      }
    }
    if (remainTime <= 0 && null != contextException) {
      throw new io.github.oitstack.goblin.runtime.exceptions.TimeoutException(contextException);
    }
  }

  public static void retryUntilTrue(long time, TimeUnit timeUnit, Callable callable)
      throws TimeoutException {
    long startTime = System.currentTimeMillis();
    long duration = timeUnit.toMillis(time);
    long remainTime = duration;
    Exception contextException = null;
    Boolean result = Boolean.FALSE;
    while (remainTime > 0) {
      try {
        result = Timeouts.<Boolean>getWithTimeout(remainTime, TimeUnit.MILLISECONDS, callable);
        if (result) {
          return;
        }
      } catch (Exception e) {
        contextException = e;
      } finally {
        remainTime = duration - System.currentTimeMillis() - startTime;
      }
    }
    if (remainTime <= 0 && null != contextException) {
      throw new io.github.oitstack.goblin.runtime.exceptions.TimeoutException(contextException);
    }
  }
}
