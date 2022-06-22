/*
 * Copyright 2022 OPPO Goblin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.oitstack.goblin.core;

import io.github.oitstack.goblin.core.utils.ConfigParseUtils;
import io.github.oitstack.goblin.core.utils.EnvExtUtils;
import io.github.oitstack.goblin.core.utils.LoggerPrinter;
import io.github.oitstack.goblin.runtime.utils.PropertyAndEnvUtils;
import io.github.oitstack.goblin.spi.GoblinLifeCycleInterceptorManager;
import io.github.oitstack.goblin.spi.context.Configuration;
import io.github.oitstack.goblin.spi.context.GoblinContext;
import io.github.oitstack.goblin.spi.context.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Goblin class.
 */
public class Goblin {
    private final static Logger LOGGER = LoggerFactory.getLogger(Goblin.class);

    /**
     * User-configured properties.
     */
    private Map<String, Object> properties;

    /**
     * User configuration.
     */
    private Configuration configuration;

    /**
     * The container that holds the docker container started by the user.
     */
    private Map<String, GoblinContainer> containerMap = new HashMap<>();

    /**
     * A placeholder for all container startup settings.
     */
    private Map<String, String> placeHolders = new HashMap<>();

    /**
     * The flag used to indicate whether Goblin is started.
     */
    private volatile AtomicBoolean started = new AtomicBoolean(false);

    public final static String PLACE_HOLDER_TPL = "GOBLIN_%s_%s";


    private Goblin() {
    }

    /**
     * Get a singleton Goblin instance.
     * @return
     */
    public static Goblin getInstance() {
        return GoblinInstanceHolder.INSTANCE;
    }

    static class GoblinInstanceHolder {
        public static Goblin INSTANCE = new Goblin();
    }


    /**
     * startup Goblin instance.
     * @param configuration
     */
    public void startup(Configuration configuration) {
        this.configuration = configuration;
        this.properties = configuration.getConfMap();

        if (started.compareAndSet(false, true)) {

            GoblinContext context = buildContext();

            preProcess(context);

            configEnvsAndJvmProperties();

            runContainers();

            setPlaceHoldersToJvmProperties();

            postProcess(context);

            LoggerPrinter.print();
        }
    }

    /**
     * build GoblinContext which is used for Goblin context.
     * @return
     */
    private GoblinContext buildContext() {
        return new GoblinContext(this.getPlaceHolders(), this.getConfiguration());
    }

    /**
     * The method to be executed after Goblin starts
     * @param context
     */
    private void postProcess(GoblinContext context) {
        GoblinLifeCycleInterceptorManager.getExts().forEach(processor -> {
            processor.postProcess(context);
        });
    }

    /**
     * The method to be executed before Goblin starts
     * @param context
     */
    private void preProcess(GoblinContext context) {
        GoblinLifeCycleInterceptorManager.getExts().forEach(processor -> {
            processor.preProcess(context);
        });
    }


    /**
     * Inject placeholders into the JVM for future use.
     */
    private void setPlaceHoldersToJvmProperties() {
        for (Map.Entry<String, String> entry : placeHolders.entrySet()) {
            PropertyAndEnvUtils.setProperty(entry.getKey(), entry.getValue());
        }
    }


    /**
     * Add user-configured property information to system properties.
     */
    private void addUserConfig2SysProperties() {
        Map<String, String> userDefinedProperties = configuration.getProps();

        //如果本地有配置， 以本地配置為准
        if (userDefinedProperties != null) {
            userDefinedProperties.forEach(PropertyAndEnvUtils::setProperty);
        }
    }


    /**
     * Extends environment variables by allowing users to configure environment variables.
     */
    private void readUserDefinedEnvsFromConfig() {
        if (null != configuration.getEnv()) {
            EnvExtUtils.addAll(configuration.getEnv());
        }
    }

    /**
     * Get placeholders.
     * @return
     */
    public Map<String, String> getPlaceHolders() {
        return this.placeHolders;
    }


    private void configEnvsAndJvmProperties() {
        readUserDefinedEnvsFromConfig();
        EnvExtUtils.injectEnv2Os();
        addUserConfig2SysProperties();
    }

    /**
     * run docker containers.
     */
    private void runContainers() {
        HashMap<String, GoblinContainer> containers = new HashMap(16);
        for (GoblinContainer container : ServiceLoader.load(GoblinContainer.class)) {
            containers.put(container.getContainerType(), container);
        }

        if (null != configuration.getDockerImages() && !containers.isEmpty()) {
            GoblinGraph<Image> containersGraph
                    = GoblinContainerDependencyGraphBuilder.buildDependencyGraph(configuration.getDockerImages());

            containersGraph.travel().forEach(nodes -> {
                final CountDownLatch cdl = new CountDownLatch(nodes.size());

                nodes.forEach(node -> {
                    Image image = (Image) node.getValue();
                    new Thread(() -> {
                        Thread.currentThread().setName("Goblin-Container-Bootstratp-" + image.getId());
                        GoblinContainer container = null;
                        try {
                            Class<?> cls = Class.forName(containers.get(image.getType()).getClass().getName());
                            container = (GoblinContainer) cls.newInstance();
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        if (null == container) {
                            cdl.countDown();
                            throw new RuntimeException(
                                    String.format("must be at least one impl of %s", image.getType()));
                        }

                        container.start(
                                this,
                                null == container.configClass() ?
                                        null : ConfigParseUtils.map2Pojo(properties, container.configClass()),
                                image
                        );
                        container.getPlaceHolders().entrySet().stream().forEach(
                                e -> placeHolders.put(String.format(PLACE_HOLDER_TPL, image.getId().toUpperCase(), e.getKey()), e.getValue())
                        );
                        containerMap.put(image.getId(), container);
                        cdl.countDown();
                    }).start();
                });

                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    LOGGER.error("goblin startup failed", e);
                    return;
                }
            });

            placeHolders = Collections.unmodifiableMap(placeHolders);
            containerMap = Collections.unmodifiableMap(containerMap);
        }
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Map<String, GoblinContainer> getContainerMap() {
        return containerMap;
    }

    /**
     * Get container instance based on container type
     * @param type
     * @return
     */
    public GoblinContainer getContainerInstance(String type) {
        return null != type && this.containerMap.containsKey(type) ? this.containerMap.get(type) : null;
    }
}
