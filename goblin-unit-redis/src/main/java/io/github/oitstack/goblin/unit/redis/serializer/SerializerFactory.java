package io.github.oitstack.goblin.unit.redis.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author CuttleFish
 * @Date 2022/7/19 上午10:52
 */
public class SerializerFactory {

    private Map<String, Serializer> serializerMap = new HashMap<>();
    private Serializer defaultSerializer = new DefaultSerializer();

    public static SerializerFactory getInstance() {
        return SerializerFactoryHolder.sf;
    }

    public Serializer getSerializer(String serializerName) {
        return serializerMap.getOrDefault(serializerName, defaultSerializer);
    }

    private SerializerFactory() {
        serializerMap.put("binaryStr2ByteArraySerializer", new BinaryStr2ByteArraySerializer());
        serializerMap.put("default", new DefaultSerializer());

    }

    static class SerializerFactoryHolder {
        static final SerializerFactory sf = new SerializerFactory();
    }
}
