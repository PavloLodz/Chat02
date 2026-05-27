package pl.ldz.chat;

import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

public class DockerShouldWorkIT {
  @Test
  void checkDockerConnection() {
    var client = DockerClientFactory.instance().client();
    System.out.println("Docker API version: " + client.versionCmd().exec().getApiVersion());
    System.out.println("Docker is accessible!");
  }
}
