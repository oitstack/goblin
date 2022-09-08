package io.github.oitstack.goblin.container.rocketmq;

public class Config {

    private RocketMQ rocketmq;

    public RocketMQ getRocketmq() {
        return rocketmq;
    }

    public void setRocketmq(RocketMQ rocketmq) {
        this.rocketmq = rocketmq;
    }

    public static class RocketMQ {
        private String[] autoCreateTopics;


        public String[] getAutoCreateTopics() {
            return autoCreateTopics;
        }

        public void setAutoCreateTopics(String[] autoCreateTopics) {
            this.autoCreateTopics = autoCreateTopics;
        }
    }
}
