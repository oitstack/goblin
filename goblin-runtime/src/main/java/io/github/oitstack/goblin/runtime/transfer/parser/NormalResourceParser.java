package io.github.oitstack.goblin.runtime.transfer.parser;

import io.github.oitstack.goblin.runtime.utils.PathUtils;

/**
 * @Author CuttleFish
 * @Date 2022/6/6 下午8:01
 */
public class NormalResourceParser implements ResourceParser {
    @Override
    public String getResource(String resourcePath) {
        return PathUtils.formatResourceURIToFilePath(resourcePath);
    }

    @Override
    public boolean support(String resourcePath) {
        return true;
    }
}
