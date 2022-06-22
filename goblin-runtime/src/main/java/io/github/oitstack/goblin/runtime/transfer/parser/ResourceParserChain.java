package io.github.oitstack.goblin.runtime.transfer.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author CuttleFish
 * @Date 2022/6/6 下午8:08
 */
public class ResourceParserChain {
    private List<ResourceParser> parsers = new ArrayList<>();

    public static ResourceParserChain getInstance() {
        return ResourceParserChainHolder.chain;
    }

    private ResourceParserChain() {
        parsers.add(new JarResourceParser());
        parsers.add(new NormalResourceParser());
    }

    static class ResourceParserChainHolder {
        private static final ResourceParserChain chain = new ResourceParserChain();
    }

    public String start(String resourcePath) {
        for (ResourceParser p : parsers) {
            if (p.support(resourcePath)) {
                return p.getResource(resourcePath);
            }
        }
        return null;
    }
}
