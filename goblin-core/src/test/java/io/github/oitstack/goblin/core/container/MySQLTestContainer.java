package io.github.oitstack.goblin.core.container;

import io.github.oitstack.goblin.core.Goblin;
import io.github.oitstack.goblin.spi.context.Image;
import io.github.oitstack.goblin.core.GoblinContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yangguang
 * @date 2022/5/27
 */
public class MySQLTestContainer implements GoblinContainer {
    private Map<String, String> placeholders = new HashMap<>();

    public final static String CONTAINER_TYPE = "MYSQL";
    public static final String DEFAULT_PASSWORD = getPwd();
    public static final String MYSQL_ROOT_USER = "root";

    @Override
    public void start(Goblin context, Object confObj, Image image) {
        this.placeholders.put("USERNAME", MYSQL_ROOT_USER);
        this.placeholders.put("PASSWORD", DEFAULT_PASSWORD);
    }

    public static String getPwd() {
        return "PASSWORD";
    }
    @Override
    public Map<String, String> getPlaceHolders() {
        return this.placeholders;
    }

    @Override
    public String getContainerType() {
        return CONTAINER_TYPE;
    }

    @Override
    public Class configClass() {
        return Config.class;
    }


}
