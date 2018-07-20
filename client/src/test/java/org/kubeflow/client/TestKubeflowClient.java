package org.kubeflow.client;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.kubeflow.client.exception.KubeflowException;
import org.kubeflow.client.model.Job;
import org.kubeflow.client.model.TFReplica;
import org.kubeflow.client.storage.HDFSStorage;

public class TestKubeflowClient {
  private KubeflowClient client;

  @Before
  public void init() throws IOException {
    this.client = KubeflowClientFactory.newInstance();
  }

  @Test(expected = KubeflowException.class)
  public void testSubmitWithNoStorageBackend() throws KubeflowException, IOException {
    Job job = new Job();
    this.client.submitJob(job);
  }

  @Test(expected = KubeflowException.class)
  public void testSubmitInvalidJob() throws KubeflowException, IOException {
    this.client.storage(new HDFSStorage());
    Job job = new Job();
    this.client.submitJob(job);
  }

  @Test(expected = IOException.class)
  public void testJobWithInvalidLocalScriptPath() throws KubeflowException, IOException {
    this.client.storage(new HDFSStorage().defaultFS("hdfs://localhost:9000"));
    Job job =
        new Job()
            .name("test")
            .script("local-fake")
            .user("me")
            .ps(new TFReplica().replicas(1))
            .worker(new TFReplica().replicas(1));
    this.client.submitJob(job);
  }
}
