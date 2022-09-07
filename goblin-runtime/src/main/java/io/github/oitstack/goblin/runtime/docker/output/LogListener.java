package io.github.oitstack.goblin.runtime.docker.output;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class LogListener extends BaseListener{
    private final Logger logger;
    private final Map<String, String> mdc = new HashMap<>();
    private boolean separateOutputStreams;
    private String prefix = "";

    public LogListener(Logger logger) {
        this(logger, false);
    }

    public LogListener(Logger logger, boolean separateOutputStreams) {
        this.logger = logger;
        this.separateOutputStreams = separateOutputStreams;
    }

    public LogListener withPrefix(String prefix) {
        this.prefix = "[" + prefix + "] ";
        return this;
    }

    public LogListener withMdc(String key, String value) {
        mdc.put(key, value);
        return this;
    }

    public LogListener withMdc(Map<String, String> mdc) {
        this.mdc.putAll(mdc);
        return this;
    }

    public LogListener withSeparateOutputStreams() {
        this.separateOutputStreams = true;
        return this;
    }

    @Override
    public void accept(OutputFrame outputFrame) {
        OutputFrame.OutputType outputType = outputFrame.getType();

        String utf8String = outputFrame.getUtf8String();
        utf8String = utf8String.replaceAll(ExecResultCallback.LINE_BREAK_AT_END_REGEX, "");

        Map<String, String> originalMdc = MDC.getCopyOfContextMap();
        MDC.setContextMap(mdc);
        try {
            switch (outputType) {
                case END:
                    break;
                case STDOUT:
                    if (separateOutputStreams) {
                        logger.info("{}{}", prefix.isEmpty() ? "" : (prefix + ": "), utf8String);
                    } else {
                        logger.info("{}{}: {}", prefix, outputType, utf8String);
                    }
                    break;
                case STDERR:
                    if (separateOutputStreams) {
                        logger.error("{}{}", prefix.isEmpty() ? "" : (prefix + ": "), utf8String);
                    } else {
                        logger.info("{}{}: {}", prefix, outputType, utf8String);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected outputType " + outputType);
            }
        } finally {
            if (originalMdc == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(originalMdc);
            }
        }
    }
}
