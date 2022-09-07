package io.github.oitstack.goblin.runtime.docker.output;

import com.github.dockerjava.api.model.StreamType;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

public abstract class BaseListener implements Consumer<OutputFrame> {
    private static AtomicLong la=new AtomicLong(0);
    private String listenerName;
    public BaseListener(){
        listenerName="#OUTPUT_LISTENER_"+la.incrementAndGet();
    }

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }
}
