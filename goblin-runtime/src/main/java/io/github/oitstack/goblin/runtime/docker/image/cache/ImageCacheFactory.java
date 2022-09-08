package io.github.oitstack.goblin.runtime.docker.image.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author CuttleFish
 * @Date 2022/6/9 下午5:26
 */
public class ImageCacheFactory {
    private Map<String, IImageCache> caches = new HashMap<>();

    public void resistCache(String name, IImageCache cache) {
        caches.put(name, cache);
    }

    public IImageCache get(String name) {
        return caches.getOrDefault(name, ImageCache.getInstance());
    }


}
