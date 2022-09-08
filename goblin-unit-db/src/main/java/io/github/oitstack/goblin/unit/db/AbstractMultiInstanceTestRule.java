package io.github.oitstack.goblin.unit.db;

import io.github.oitstack.goblin.spi.testrule.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultiInstanceTestRule implements TestRule {

    public final static Logger logger = LoggerFactory.getLogger(AbstractMultiInstanceTestRule.class);
    private static final String NAME = "default";
    private static final String EXTENSION = "json";
    private LoadStrategyFactory loadStrategyFactory = new ReflectionLoadStrategyFactory();


    protected DefaultDataSetLocationResolver defaultDataSetLocationResolver;

    public abstract String getWorkingExtension();

    protected abstract DatabaseOperation getDatabaseOperation(String instanceId);

    public abstract String getName();

    @Override
    public void beforeAll(TestContext testContext) {
        defaultDataSetLocationResolver = new DefaultDataSetLocationResolver(testContext);

        UsingDataSet usingDataSet = testContext.getTestClass().getAnnotation(UsingDataSet.class);
        if (isTestAnnotatedWithUsingDataSet(usingDataSet)) {
            String[] instanceIds = usingDataSet.ids();
            for (String instanceId : instanceIds) {

                List<InputStream> datasetStream = loadDataSetAsStream(instanceId);
                if (datasetStream.isEmpty()) {
                    continue;
                }
                loadDataSets(datasetStream, LoadStrategyEnum.CLEAN_INSERT, instanceId);
            }
        }
    }

    @Override
    public void beforeEach(TestContext testContext) {
        defaultDataSetLocationResolver = new DefaultDataSetLocationResolver(testContext);

        UsingDataSet usingDataSet = testContext.getTestMethod().getAnnotation(UsingDataSet.class);
        if (isTestAnnotatedWithUsingDataSet(usingDataSet)) {
            String[] instanceIds = usingDataSet.ids();
            for (String instanceId : instanceIds) {

                List<InputStream> datasetStream = loadDataSetAsStream(instanceId);
                if (datasetStream.isEmpty()) {
                    continue;
                }
                loadDataSets(datasetStream, LoadStrategyEnum.CLEAN_INSERT, instanceId);
            }
        }
    }

    @Override
    public void afterEach(TestContext testContext) {
        defaultDataSetLocationResolver = new DefaultDataSetLocationResolver(testContext);

        ShouldMatchDataSet shouldMatchDataSet = testContext.getTestMethod().getAnnotation(ShouldMatchDataSet.class);
        if (isTestAnnotatedWithShouldMatchDataSet(shouldMatchDataSet)) {

            String[] instanceIds = shouldMatchDataSet.ids();
            for (String instanceId : instanceIds) {

                IgnorePropertyValue ignorePropertyValue = testContext.getTestMethod().getAnnotation(IgnorePropertyValue.class);
                AbstractCustomizableDatabaseOperation databaseOperation = (AbstractCustomizableDatabaseOperation) getDatabaseOperation(instanceId);
                if (null == databaseOperation) {
                    logger.warn("Can not found databaseOperation by id:" + instanceId);
                    return;
                }
                try {
                    Field comparisonStrategyField = databaseOperation.getClass().getSuperclass().getDeclaredField("comparisonStrategy");
                    comparisonStrategyField.setAccessible(true);
                    ComparisonStrategy comparisonStrategy = (ComparisonStrategy) comparisonStrategyField.get(databaseOperation);
                    if (null == ignorePropertyValue) {
                        comparisonStrategy.setIgnoreProperties(null);
                    } else {
                        comparisonStrategy.setIgnoreProperties(ignorePropertyValue.properties());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                InputStream datasetStream = loadShouldMatchDataSetAsStream(instanceId);
                if (datasetStream == null) {
                    continue;
                }
                assertExpectation(datasetStream, instanceId);

            }
        }

        UsingDataSet usingDataSet = testContext.getTestMethod().getAnnotation(UsingDataSet.class);


        if (isTestAnnotatedWithUsingDataSet(usingDataSet)) {
            String[] instanceIds = usingDataSet.ids();
            for (String instanceId : instanceIds) {

                List<InputStream> datasetStream = loadDataSetAsStream(instanceId);
                if (datasetStream.isEmpty()) {
                    continue;
                }
                loadDataSets(datasetStream, LoadStrategyEnum.DELETE_ALL, instanceId);
            }
        }
    }

    @Override
    public void afterAll(TestContext testContext) {
        defaultDataSetLocationResolver = new DefaultDataSetLocationResolver(testContext);
        UsingDataSet usingDataSet = testContext.getTestClass().getAnnotation(UsingDataSet.class);
        if (isTestAnnotatedWithUsingDataSet(usingDataSet)) {
            String[] instanceIds = usingDataSet.ids();
            for (String instanceId : instanceIds) {
                List<InputStream> datasetStream = loadDataSetAsStream(instanceId);
                if (datasetStream.isEmpty()) {
                    continue;
                }
                loadDataSets(datasetStream, LoadStrategyEnum.DELETE_ALL, instanceId);
            }

        }
    }

    public void setLoadStrategyFactory(LoadStrategyFactory loadStrategyFactory) {
        this.loadStrategyFactory = loadStrategyFactory;
    }

    public List<InputStream> loadDataSetAsStream(String instanceId) {
        List<InputStream> scriptContent = new ArrayList<InputStream>();

        String[] locations = null;
        if (null != locations && locations.length > 0) {
            for (String location : locations) {
                InputStream stream = IOUtils.getStreamFromClasspathBaseResource(this.getClass(), location);
                if (null == stream) {
                    String errMsg = "File specified in locations property are not present in classpath, or no files matching default name are found. " +
                            "Valid default locations are: " + location;
                    logger.error(errMsg);
                    throw new IllegalArgumentException(errMsg);
                }
                scriptContent.add(stream);
            }
        } else {
            String defaultLocation = defaultDataSetLocationResolver.resolveDefaultDataSetLocation("-" + getName() + "#" + instanceId + "." + getWorkingExtension());
            InputStream stream = IOUtils.getStreamFromClasspathBaseResource(defaultDataSetLocationResolver.getResourceBase(), defaultLocation);

            if (null == stream) {
                String errMsg = "File specified in locations property are not present in classpath, or no files matching default name are found. " +
                        "Valid default locations are: " + defaultLocation;
                logger.info(errMsg);
//                throw new IllegalArgumentException(errMsg);
            } else {
                scriptContent.add(stream);
            }
        }

        return scriptContent;
    }

    public InputStream loadShouldMatchDataSetAsStream(String instanceId) {
        InputStream scriptContent;

        String location = null;
        if (null != location && !location.isEmpty()) {
            scriptContent = IOUtils.getStreamFromClasspathBaseResource(this.getClass(), location);
            if (null == scriptContent) {
                String errMsg = "File specified in locations property are not present in classpath, or no files matching default name are found. " +
                        "Valid default locations are: " + location;
                logger.error(errMsg);
                throw new IllegalArgumentException(errMsg);
            }

        } else {
            String defaultLocation = defaultDataSetLocationResolver.resolveDefaultDataSetLocation("-" + getName() + "#" + instanceId + "-expected." + getWorkingExtension());
            scriptContent = IOUtils.getStreamFromClasspathBaseResource(defaultDataSetLocationResolver.getResourceBase(), defaultLocation);

            if (null == scriptContent) {
                String errMsg = "No files matching default name are found. " +
                        "Valid default locations are: " + defaultLocation;
                logger.info(errMsg);
            }
        }

        return scriptContent;
    }

    public void loadDataSets(List<InputStream> datasetStream, LoadStrategyEnum loadStrategyEnum, String instanceId) {
        DatabaseOperation databaseOperation = getDatabaseOperation(instanceId);
        if (null == databaseOperation) {
            logger.warn("Can not found databaseOperation by id:" + instanceId);
            return;
        }

        LoadStrategyOperation loadStrategyOperation = loadStrategyFactory.getLoadStrategyInstance(loadStrategyEnum, databaseOperation);
        loadStrategyOperation.executeScripts(datasetStream
                .toArray(new InputStream[datasetStream.size()]));
    }

    private void assertExpectation(InputStream datasetStream, String instanceId) {
        DatabaseOperation databaseOperation = getDatabaseOperation(instanceId);
        if (null == databaseOperation) {
            logger.warn("Can not found databaseOperation by id:" + instanceId);
            return;
        }
        databaseOperation.databaseIs(datasetStream);
    }

    protected boolean isTestAnnotatedWithUsingDataSet(UsingDataSet annotation) {
        return null != annotation && null != annotation.ids() && annotation.ids().length > 0;
    }

    protected boolean isTestAnnotatedWithShouldMatchDataSet(ShouldMatchDataSet annotation) {
        return null != annotation && null != annotation.ids() && annotation.ids().length > 0;
    }
}
