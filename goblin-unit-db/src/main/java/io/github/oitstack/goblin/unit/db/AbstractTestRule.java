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
package io.github.oitstack.goblin.unit.db;

import io.github.oitstack.goblin.spi.testrule.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a template class for test rule, you can inherit this class to customize your own test rule.
 */
public abstract class AbstractTestRule implements TestRule {

    public final static Logger LOGGER = LoggerFactory.getLogger(AbstractTestRule.class);

    /**
     * Data operation strategy factory.
     */
    private LoadStrategyFactory loadStrategyFactory = new ReflectLoadStrategyFactory();

    /**
     * DataBase operations.
     */
    protected DatabaseOperation databaseOperation;

    protected DefaultDataSetLocationResolver defaultDataSetLocationResolver;

    /**
     * Template method to get the suffix of the test data file.
     */
    public abstract String getWorkingExtension();

    /**
     * Template method to get database operations.
     */
    public abstract DatabaseOperation getDatabaseOperation();

    /**
     * Template method to Get name of test rule.
     */
    public abstract String getName();

    /**
     * This method is executed before all use cases are executed.
     * Read whether there is a UsingDataSet annotation on current test class, if so,
     * read the data of the configuration file and insert it into the database.
     * @param testContext Context information on test cases encapsulated by spring-test.
     */
    @Override
    public void beforeAll(TestContext testContext) {
        defaultDataSetLocationResolver = new DefaultDataSetLocationResolver(testContext);

        UsingDataSet usingDataSet = testContext.getTestClass().getAnnotation(UsingDataSet.class);
        if (isTestAnnotatedWithUsingDataSet(usingDataSet)) {
            List<InputStream> datasetStream = loadDataSetAsStream(usingDataSet);
            if (datasetStream.isEmpty()) {
                return;
            }
            loadDataSets(datasetStream, LoadStrategyEnum.CLEAN_INSERT);
        }
    }

    /**
     * This method is executed before current use cases are executed.
     * Read whether there is a UsingDataSet annotation on the current test class,
     * if so, read the data in the configuration file and insert it into the database.
     * @param testContext Context information on test cases encapsulated by spring-test.
     */
    @Override
    public void beforeEach(TestContext testContext) {
        defaultDataSetLocationResolver = new DefaultDataSetLocationResolver(testContext);

        UsingDataSet usingDataSet = testContext.getTestMethod().getAnnotation(UsingDataSet.class);
        if (isTestAnnotatedWithUsingDataSet(usingDataSet)) {
            List<InputStream> datasetStream = loadDataSetAsStream(usingDataSet);
            if (datasetStream.isEmpty()) {
                return;
            }
            loadDataSets(datasetStream, LoadStrategyEnum.CLEAN_INSERT);
        }
    }

    /**
     * This method is executed after current use cases are executed.
     * Read whether there is a ShouldMatchDataSet annotation on the current test method,
     * and if so, check whether the data in the database is correct.
     * Read whether there is a UsingDataSet annotation on the current test method, if so, delete all data in the database.
     * @param testContext Context information on test cases encapsulated by spring-test.
     */
    @Override
    public void afterEach(TestContext testContext) {
        defaultDataSetLocationResolver = new DefaultDataSetLocationResolver(testContext);

        ShouldMatchDataSet shouldMatchDataSet = testContext.getTestMethod().getAnnotation(ShouldMatchDataSet.class);
        if (isTestAnnotatedWithShouldMatchDataSet(shouldMatchDataSet)) {

            IgnorePropertyValue ignorePropertyValue = testContext.getTestMethod().getAnnotation(IgnorePropertyValue.class);
            AbstractCustomizableDatabaseOperation databaseOperation = (AbstractCustomizableDatabaseOperation) getDatabaseOperation();

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

            InputStream datasetStream = loadShouldMatchDataSetAsStream(shouldMatchDataSet);
            if (datasetStream == null) {
                return;
            }
            assertExpectation(datasetStream);
        }

        UsingDataSet usingDataSet = testContext.getTestMethod().getAnnotation(UsingDataSet.class);
        if (isTestAnnotatedWithUsingDataSet(usingDataSet)) {
            List<InputStream> datasetStream = loadDataSetAsStream(usingDataSet);
            if (datasetStream.isEmpty()) {
                return;
            }
            loadDataSets(datasetStream, LoadStrategyEnum.DELETE_ALL);
        }
    }

    /**
     * This method is executed after all use cases are executed.
     * @param testContext Context information on test cases encapsulated by spring-test.
     */
    @Override
    public void afterAll(TestContext testContext) {
        defaultDataSetLocationResolver = new DefaultDataSetLocationResolver(testContext);
        UsingDataSet usingDataSet = testContext.getTestClass().getAnnotation(UsingDataSet.class);
        if (isTestAnnotatedWithUsingDataSet(usingDataSet)) {
            List<InputStream> datasetStream = loadDataSetAsStream(usingDataSet);
            if (datasetStream.isEmpty()) {
                return;
            }
            loadDataSets(datasetStream, LoadStrategyEnum.DELETE_ALL);
        }
    }

    /**
     * Set Data operation strategy factory.
     * @param loadStrategyFactory
     */
    public void setLoadStrategyFactory(LoadStrategyFactory loadStrategyFactory) {
        this.loadStrategyFactory = loadStrategyFactory;
    }

    /**
     * Read data from configuration file
     * @param usingDataSet  Annotations on test classes or test cases
     * @return
     */
    public List<InputStream> loadDataSetAsStream(UsingDataSet usingDataSet) {
        List<InputStream> scriptContent = new ArrayList<InputStream>();

        String[] locations = null;
        if (null != locations && locations.length > 0) {
            for (String location : locations) {
                InputStream stream = IOUtils.getStreamFromClassPathBaseResource(this.getClass(), location);
                if (null == stream) {
                    String errMsg = "File specified in locations property are not present in classpath, or no files matching default name are found. " +
                            "Valid default locations are: " + location;
                    LOGGER.error(errMsg);
                    throw new IllegalArgumentException(errMsg);
                }
                scriptContent.add(stream);
            }
        } else {
            String defaultLocation = defaultDataSetLocationResolver.resolveDefaultDataSetLocation("-" + getName() + "." + getWorkingExtension());
            InputStream stream = IOUtils.getStreamFromClassPathBaseResource(defaultDataSetLocationResolver.getResourceBase(), defaultLocation);

            if (null == stream) {
                String errMsg = "File specified in locations property are not present in classpath, or no files matching default name are found. " +
                        "Valid default locations are: " + defaultLocation;
                LOGGER.info(errMsg);
//                throw new IllegalArgumentException(errMsg);
            } else {
                scriptContent.add(stream);
            }
        }

        return scriptContent;
    }

    public InputStream loadShouldMatchDataSetAsStream(ShouldMatchDataSet shouldMatchDataSet) {
        InputStream scriptContent;

        String location = null;
        if (null != location && !location.isEmpty()) {
            scriptContent = IOUtils.getStreamFromClassPathBaseResource(this.getClass(), location);
            if (null == scriptContent) {
                String errMsg = "File specified in locations property are not present in classpath, or no files matching default name are found. " +
                        "Valid default locations are: " + location;
                LOGGER.error(errMsg);
                throw new IllegalArgumentException(errMsg);
            }

        } else {
            String defaultLocation = defaultDataSetLocationResolver.resolveDefaultDataSetLocation("-" + getName() + "-expected." + getWorkingExtension());
            scriptContent = IOUtils.getStreamFromClassPathBaseResource(defaultDataSetLocationResolver.getResourceBase(), defaultLocation);

            if (null == scriptContent) {
                String errMsg = "No files matching default name are found. " +
                        "Valid default locations are: " + defaultLocation;
                LOGGER.info(errMsg);
            }
        }

        return scriptContent;
    }

    /**
     * Execute data script.
     */
    public void loadDataSets(List<InputStream> datasetStream, LoadStrategyEnum loadStrategyEnum) {
        LoadStrategyOperation loadStrategyOperation = loadStrategyFactory.getLoadStrategyInstance(loadStrategyEnum, getDatabaseOperation());
        loadStrategyOperation.executeScripts(datasetStream
                .toArray(new InputStream[datasetStream.size()]));
    }

    /**
     * Assert whether the data obtained from the database is consistent with the configured data.
     * @param datasetStream data to be compared with data from database.
     */
    private void assertExpectation(InputStream datasetStream) {
        getDatabaseOperation().databaseIs(datasetStream);
    }

    /**
     * Determine whether it is marked with annotations.
     */
    protected boolean isTestAnnotatedWithUsingDataSet(Annotation annotation) {
        return null != annotation;
    }

    /**
     * Determine whether it is marked with annotations.
     */
    protected boolean isTestAnnotatedWithShouldMatchDataSet(Annotation annotation) {
        return null != annotation;
    }
}
