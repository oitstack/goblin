package io.github.oitstack.goblin.container.mongodb;


import io.github.oitstack.goblin.core.Goblin;
import io.github.oitstack.goblin.core.GoblinContainer;
import io.github.oitstack.goblin.runtime.RuntimeOperation;
import io.github.oitstack.goblin.runtime.docker.container.DockerContainerAdapter;
import io.github.oitstack.goblin.runtime.docker.utils.JsonTool;
import io.github.oitstack.goblin.runtime.docker.wait.strategy.DockerLogMessageWaitStrategy;
import io.github.oitstack.goblin.runtime.wait.Wait;
import io.github.oitstack.goblin.spi.context.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GoblinMongoDbContainer extends DockerContainerAdapter implements GoblinContainer {

    //private static final Logger log = LoggerFactory.getLogger(GoblinMongoDbContainer.class);
    private Map<String, String> placeholders = new HashMap<>();

    private final static String containerType = "MONGODB";
    private final static String ROOT_USERNAME = "mongoadmin";
    private final static String ROOT_PASSWORD = "secret";

    public void start(Goblin context, Object confObj, Image image) {
        this.image(image.getImageVersion());
        this.addEnv("MONGO_INITDB_ROOT_USERNAME", ROOT_USERNAME);
        this.addEnv("MONGO_INITDB_ROOT_PASSWORD", ROOT_PASSWORD);
        //this.waitingFor(Wait.forLogMessage("(?i).*waiting for connections.*", 2));

        this.start();

        if (null != confObj) {
            Config conf = (Config) confObj;

            if (null != conf.getMongodb() && null != conf.getMongodb().getDatabase()) {
                this.placeholders.put("DATABASENAME", conf.getMongodb().getDatabase());
                this.placeholders.put("URL", String.format("mongodb://%s:%s@%s:%s/%s?authSource=admin", ROOT_USERNAME, ROOT_PASSWORD, this.getHost(), this.getPortByInnerPort(27017), conf.getMongodb().getDatabase()));
                RuntimeOperation.ExecResult execResult=this.execInRuntime(buildMongoEvalCommand(buildMongoCreateUserCommandAtDB(conf.getMongodb().getDatabase())));
        System.out.println(
            "mongodb execResult:" + JsonTool.toJSONString(execResult));
            } else {
                this.placeholders.put("URL", String.format("mongodb://%s:%s@%s:%s", ROOT_USERNAME, ROOT_PASSWORD, this.getHost(), this.getPortByInnerPort(27017)));
            }
        }

        this.placeholders.put("USERNAME", ROOT_USERNAME);
        this.placeholders.put("PASSWORD", ROOT_PASSWORD);
        this.placeholders.put("HOST", this.getHost());
        this.placeholders.put("PORT", String.valueOf(this.getPortByInnerPort(27017)));
    }

    @Override
    public Map<String, String> getPlaceHolders() {
        return this.placeholders;
    }

    @Override
    public String getContainerType() {
        return containerType;
    }

    @Override
    public Class configClass() {
        return Config.class;
    }

    @Override
    protected void blockUntilContainerStarted() {
        //FIXME:
        super.blockUntilContainerStarted();
        new DockerLogMessageWaitStrategy().withRegEx("(?i).*waiting for connections.*").withTimes(2).waitUntilReady(this);
    }

    private String[] buildMongoEvalCommand(final String command) {
        return new String[]{"mongo", "-u",  ROOT_USERNAME, "-p", ROOT_PASSWORD, "--eval", command};
    }

    private String buildMongoCreateUserCommandAtDB(String dbname) {
        return String.format("db.getSiblingDB('%s').createUser({user:'%s', pwd:'%s', roles:[{role:'dbOwner',db:'%s'}]})", dbname, ROOT_USERNAME, ROOT_PASSWORD, dbname);
    }
}

