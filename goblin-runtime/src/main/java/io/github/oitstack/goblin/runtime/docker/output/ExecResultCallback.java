/*
 * Copyright 2022 OPPO Goblin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.oitstack.goblin.runtime.docker.output;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author CuttleFish @Date 2022/3/9 下午4:00
 */
@Data
public class ExecResultCallback extends ResultCallbackTemplate<ExecResultCallback, Frame> {
  private OutputFrame brokenFrame;
  private StringBuilder logString = new StringBuilder();
  private static final String LINE_BREAK_REGEX = "((\\r?\\n)|(\\r))";

  static final String LINE_BREAK_AT_END_REGEX = LINE_BREAK_REGEX + "$";
  private static final byte[] EMPTY_LINE = new byte[0];

  private ConcurrentHashMap<OutputFrame.OutputType, CopyOnWriteArrayList<BaseListener>> listeners =
      new ConcurrentHashMap<>();

  public void register(OutputFrame.OutputType type, BaseListener listener) {
    List<BaseListener> matchedList =
        listeners.computeIfAbsent(
            type,
            k -> {
              return new CopyOnWriteArrayList<BaseListener>();
            });

    matchedList.add(listener);
  }

  public void unRegister(OutputFrame.OutputType type, BaseListener listener) {}

  @Override
  public void onNext(Frame frame) {

    OutputFrame outputFrame = OutputFrame.forFrame(frame);
    if (null == outputFrame) {
      return;
    }

    List<BaseListener> matchListeners = listeners.get(outputFrame.getType());

    if (null == matchListeners || matchListeners.isEmpty()) {
      return;
    }
    if (frame.getStreamType() == StreamType.RAW) {
      processRawFrame(outputFrame, matchListeners);
    } else {
      processOtherFrame(outputFrame, matchListeners);
    }
  }

  private synchronized void processOtherFrame(
      OutputFrame outputFrame, List<BaseListener> matchListeners) {
    String utf8String = outputFrame.getUtf8String();
    matchListeners.forEach(
        l -> new OutputFrame(OutputFrame.OutputType.STDOUT, utf8String.getBytes()));
  }

  private synchronized void processRawFrame(
      OutputFrame outputFrame, List<BaseListener> matchListeners) {
    String utf8String = outputFrame.getUtf8String();
    byte[] bytes = outputFrame.getBytes();

    // Merging the strings by bytes to solve the problem breaking non-latin unicode symbols.
    if (brokenFrame != null) {
      bytes = merge(brokenFrame.getBytes(), bytes);
      utf8String = new String(bytes);
      brokenFrame = null;
    }
    // Logger chunks can break the string in middle of multibyte unicode character.
    // Backup the bytes to reconstruct proper char sequence with bytes from next frame.
    int lastCharacterType = Character.getType(utf8String.charAt(utf8String.length() - 1));
    if (lastCharacterType == Character.OTHER_SYMBOL) {
      brokenFrame = new OutputFrame(outputFrame.getType(), bytes);
      return;
    }

    //        utf8String = processAnsiColorCodes(utf8String, consumer);
    normalizeLogLines(utf8String, matchListeners);
  }
  //    private String processAnsiColorCodes(String utf8String, Consumer<OutputFrame> consumer) {
  //        if (!(consumer instanceof BaseConsumer) || ((BaseConsumer)
  // consumer).isRemoveColorCodes()) {
  //            return ANSI_COLOR_PATTERN.matcher(utf8String).replaceAll("");
  //        }
  //        return utf8String;
  //    }
  private void normalizeLogLines(String utf8String, List<BaseListener> matchListeners) {
    // Reformat strings to normalize new lines.
    List<String> lines = new ArrayList<>(Arrays.asList(utf8String.split(LINE_BREAK_REGEX)));
    if (lines.isEmpty()) {
      matchListeners.forEach(
          l -> l.accept(new OutputFrame(OutputFrame.OutputType.STDOUT, EMPTY_LINE)));
      return;
    }
    if (utf8String.startsWith("\n") || utf8String.startsWith("\r")) {
      lines.add(0, "");
    }
    if (utf8String.endsWith("\n") || utf8String.endsWith("\r")) {
      lines.add("");
    }
    for (int i = 0; i < lines.size() - 1; i++) {
      String line = lines.get(i);
      if (i == 0 && logString.length() > 0) {
        line = logString.toString() + line;
        logString.setLength(0);
      }
      String fLine = line;
      matchListeners.forEach(l -> new OutputFrame(OutputFrame.OutputType.STDOUT, fLine.getBytes()));
    }
    logString.append(lines.get(lines.size() - 1));
  }

  private byte[] merge(byte[] str1, byte[] str2) {
    byte[] mergedString = new byte[str1.length + str2.length];
    System.arraycopy(str1, 0, mergedString, 0, str1.length);
    System.arraycopy(str2, 0, mergedString, str1.length, str2.length);
    return mergedString;
  }
}
