package io.github.oitstack.goblin.unit.redis.serializer;


import io.github.oitstack.goblin.unit.redis.parser.JsonToJedisConverter;

/**
 * @Author CuttleFish
 * @Date 2022/7/19 上午10:55
 */
public class DefaultSerializer implements Serializer {
    @Override
    public byte[] serialize(String str) {
        return JsonToJedisConverter.toByteArray(str);
    }
}
