package io.github.oitstack.goblin.unit.redis.serializer;

/**
 * @Author CuttleFish
 * @Date 2022/7/19 上午10:51
 */
public interface Serializer {
    public byte[] serialize(String str);

}
