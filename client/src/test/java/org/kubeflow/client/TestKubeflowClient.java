package org.kubeflow.client;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.kubeflow.client.exception.ClientException;
import org.kubeflow.client.model.Job;
import org.kubeflow.client.storage.HDFSStorage;

public class TestKubeflowClient {
  private KubeflowClient client;

  @Before
  public void init() throws IOException {
    this.client = KubeflowClientFactory.newInstance();
  }

  @Test(expected = ClientException.class)
  public void testSubmitWithNoStorageBackend() throws ClientException, IOException {
    Job job = new Job();
    this.client.submitJob(job);
  }

  @Test(expected = ClientException.class)
  public void testSubmitInvalidJob() throws ClientException, IOException {
    this.client.storage(new HDFSStorage());
    Job job = new Job();
    this.client.submitJob(job);
  }

  @Test(expected = IOException.class)
  public void testJobWithInvalidLocalScriptPath() throws ClientException, IOException {
    this.client.storage(new HDFSStorage("hdfs://localhost:9000"));
    Job job = new Job().name("test").script("local-fake").user("me");
    this.client.submitJob(job);
  }
}
