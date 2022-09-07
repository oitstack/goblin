package io.github.oitstack.goblin.runtime.docker.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class WaitingListener extends BaseListener {

  private LinkedBlockingDeque<OutputFrame> frames = new LinkedBlockingDeque<>();

  @Override
  public void accept(OutputFrame frame) {
    frames.add(frame);
  }

  public LinkedBlockingDeque<OutputFrame> getFrames() {
    return frames;
  }

  public void waitUntil(Predicate<OutputFrame> predicate) throws TimeoutException {
    // ~2.9 million centuries ought to be enough for anyone
    waitUntil(predicate, Long.MAX_VALUE, 1);
  }

  public void waitUntil(Predicate<OutputFrame> predicate, int limit, TimeUnit limitUnit)
      throws TimeoutException {
    waitUntil(predicate, limit, limitUnit, 1);
  }

  public void waitUntil(Predicate<OutputFrame> predicate, long limit, TimeUnit limitUnit, int times)
      throws TimeoutException {
    long expiry = limitUnit.toMillis(limit) + System.currentTimeMillis();

    waitUntil(predicate, expiry, times);
  }

  private void waitUntil(Predicate<OutputFrame> predicate, long expiry, int times)
      throws TimeoutException {

    int numberOfMatches = 0;
    while (System.currentTimeMillis() < expiry) {
      try {
        OutputFrame frame = frames.pollLast(100, TimeUnit.MILLISECONDS);

        if (frame != null) {
          final String trimmedFrameText = frame.getUtf8String().replaceFirst("\n$", "");

          if (predicate.test(frame)) {
            numberOfMatches++;

            if (numberOfMatches == times) {
              return;
            }
          }
        }

        if (frames.isEmpty()) {
          // sleep for a moment to avoid excessive CPU spinning
          Thread.sleep(10L);
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    // did not return before expiry was reached
    throw new TimeoutException();
  }

  public void waitUntilEnd() {
    try {
      waitUntilEnd(Long.MAX_VALUE);
    } catch (TimeoutException e) {
      // timeout condition can never occur in a realistic timeframe
      throw new IllegalStateException(e);
    }
  }

  public void waitUntilEnd(long limit, TimeUnit limitUnit) throws TimeoutException {
    long expiry = limitUnit.toMillis(limit) + System.currentTimeMillis();

    waitUntilEnd(expiry);
  }

  private void waitUntilEnd(Long expiry) throws TimeoutException {
    while (System.currentTimeMillis() < expiry) {
      try {
        OutputFrame frame = frames.pollLast(100, TimeUnit.MILLISECONDS);

        if (frame == OutputFrame.END) {
          return;
        }

        if (frames.isEmpty()) {
          // sleep for a moment to avoid excessive CPU spinning
          Thread.sleep(10L);
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    throw new TimeoutException("Expiry time reached before end of output");
  }
}
