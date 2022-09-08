package io.github.oitstack.goblin.container.mockserver;

import io.github.oitstack.goblin.core.Goblin;
import io.github.oitstack.goblin.core.GoblinContainer;
import io.github.oitstack.goblin.runtime.docker.container.DockerContainerAdapter;
import io.github.oitstack.goblin.runtime.wait.Wait;
import io.github.oitstack.goblin.spi.context.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GoblinMockServerContainer<SELF extends GoblinMockServerContainer<SELF>>
    extends DockerContainerAdapter<SELF> implements GoblinContainer {
  private static final Logger log = LoggerFactory.getLogger(GoblinMockServerContainer.class);

  public static final int PORT = 1080;
  private Map<String, String> placeholders = new HashMap<>();

  private static final String containerType = "MOCKSERVER";

  @Override
  public void start(Goblin goblin, Object config, Image image) {
    this.image(image.getImageVersion());

    this.addCommandWhenStartup("-logLevel INFO -serverPort 1080")
        .exposedPorts(new Integer[] {Integer.valueOf(PORT)})
        .start();

    this.placeholders.put("HOST", this.getHost());
    this.placeholders.put("PORT", String.valueOf(this.getServerPort()));
    this.placeholders.put("ENDPOINT", this.getEndpoint());
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
    return null;
  }

  @Override
  protected void blockUntilContainerStarted() {
    super.blockUntilContainerStarted();
    Wait.forHttp("/mockserver/status").withMethod("PUT").forStatusCode(200);
  }

  public String getEndpoint() {
    return String.format("http://%s:%d", this.getHost(), this.getPortByInnerPort(1080));
  }

  public Integer getServerPort() {
    return this.getPortByInnerPort(1080);
  }
}
