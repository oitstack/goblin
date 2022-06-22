package io.github.oitstack.goblin.runtime.transfer.parser;

/**
 * @Author CuttleFish
 * @Date 2022/6/6 下午7:59
 */
public interface ResourceParser {
    String getResource(String resourcePath);

    boolean support(String resourcePath);
}
