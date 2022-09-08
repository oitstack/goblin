package io.github.oitstack.goblin.unit.elasticsearch;


import io.github.oitstack.goblin.core.Goblin;
import io.github.oitstack.goblin.core.GoblinContainer;
import io.github.oitstack.goblin.unit.db.AbstractMultiInstanceTestRule;
import io.github.oitstack.goblin.unit.db.DatabaseOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ElasticsearchTestRule extends AbstractMultiInstanceTestRule {
    private static final String NAME = "es";
    private static final String EXTENSION = "json";
    private Map<String, DatabaseOperation> databaseOperations = new HashMap<>();
    private Map<String, ElasticsearchConfiguration> esConfigurationMap = new HashMap<>();

    public ElasticsearchTestRule() {
        Map<String, GoblinContainer> containers = Goblin.getInstance().getContainerMapByType("ELASTICSEARCH");
        if (null == containers) {
            return;
        }

        esConfigurationMap = containers.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> new ElasticsearchConfiguration(
                                e.getValue().getPlaceHolders().get("HOST"),
                                Integer.valueOf(e.getValue().getPlaceHolders().get("PORT"))
                        )
                ));

        databaseOperations = esConfigurationMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> new ElasticsearchOperation(e.getValue())
                ));
    }

    public String getWorkingExtension() {
        return EXTENSION;
    }

    @Override
    protected DatabaseOperation getDatabaseOperation(String instanceId) {
        return databaseOperations.get(instanceId);
    }

    public String getName() {
        return NAME;
    }

}
