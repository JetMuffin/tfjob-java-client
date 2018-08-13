package org.kubeflow.client.model;

import static org.junit.Assert.*;
import static org.kubeflow.client.model.JobConstants.DEFAULT_CLEANUP_TTL_SECONDS;
import static org.kubeflow.client.model.JobConstants.KUBEFLOW_API_VERSION;
import static org.kubeflow.client.model.JobConstants.KUBEFLOW_JOB_KIND;

import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1EnvVar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.kubeflow.client.models.V1alpha2TFJob;
import org.kubeflow.client.models.V1alpha2TFReplicaSpec;

public class JobTest {

  @Test
  public void testBuildWithDefaultParams() {
    Job job = new Job();

    V1alpha2TFJob tfjob = job.getTfjob();
    assertEquals(tfjob.getApiVersion(), KUBEFLOW_API_VERSION);
    assertEquals(
        tfjob.getSpec().getTtlSecondsAfterFinishing().intValue(), DEFAULT_CLEANUP_TTL_SECONDS);
    assertEquals(tfjob.getKind(), KUBEFLOW_JOB_KIND);
  }

  @Test
  public void testBuildWithParams() {
    TFReplica ps =
        new TFReplica()
            .replicas(1)
            .cpu(0.5)
            .memory(1024.0)
            .image("tensorflow:1.7.0")
            .command("python test.py")
            .args("--train_tfrecords=train --test_tfrecords=test")
            .env("RESOURCE_PATH", "file:///data");
    TFReplica worker =
        new TFReplica()
            .replicas(1)
            .cpu(0.5)
            .memory(1024.0)
            .image("tensorflow:1.7.0")
            .command("python test.py")
            .args("--train_tfrecords=train --test_tfrecords=test")
            .env("RESOURCE_PATH", "file:///data");
    Job job = new Job().name("test_job").ps(ps).worker(worker).ttlSecondsAfterFinishing(3600);

    V1alpha2TFJob tfjob = job.getTfjob();

    assertEquals(
        tfjob.getSpec().getTtlSecondsAfterFinishing().intValue(),
        job.getTtlSecondsAfterFinishing());

    assertEquals(tfjob.getMetadata().getName(), job.getName());
    assert tfjob.getSpec().getTfReplicaSpecs().containsKey("PS");
    assert tfjob.getSpec().getTfReplicaSpecs().containsKey("Worker");

    V1alpha2TFReplicaSpec psSpec = tfjob.getSpec().getTfReplicaSpecs().get("PS");

    boolean found = false;
    for (V1Container container : psSpec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        found = true;
        assertEquals(container.getImage(), "tensorflow:1.7.0");
      }
    }
    assert found;

    assertEquals(psSpec.getReplicas().intValue(), job.getPs().getReplicas());

    assert psSpec.getTemplate().getSpec().getContainers().size() > 0;
    V1Container container = psSpec.getTemplate().getSpec().getContainers().get(0);

    assertEquals(container.getImage(), job.getPs().getImage());
    assertEquals(container.getCommand().size(), 2);
    assertEquals(container.getCommand().get(0), "python");
    assertEquals(container.getCommand().get(1), "test.py");

    assertEquals(container.getArgs().size(), 2);
    assertEquals(container.getArgs().get(0), "--train_tfrecords=train");

    List<V1EnvVar> envVar = container.getEnv();
    Map<String, String> envs = new HashMap<>();
    for (V1EnvVar item : envVar) {
      envs.put(item.getName(), item.getValue());
    }
    assert envs.containsKey("RESOURCE_PATH");
    assertEquals(envs.get("RESOURCE_PATH"), "file:///data");
  }

  @Test
  public void testJobUUID() {
    Job job = new Job();
    assertNotNull(job.getUUID());
  }

  @Test
  public void testInvalidJobToGetRemoteScriptPath() {
    Job job = new Job();
    assertNull(job.getRemoteScriptPath("/sigma"));
  }

  @Test
  public void testValidJobToGetRemoteScriptPath() {
    Job job =
        new Job().user("usertest").name("train").namespace("work").script("/tmp/train.tar.gz");
    String expect = "/sigma/usertest/work/tfjob/" + job.getUUID() + "/train.tar.gz";

    assertEquals(expect, job.getRemoteScriptPath("/sigma"));
  }

  @Test
  public void testJobWithDefaultSettings() {
    Job job = new Job();

    assertEquals(job.getUser(), System.getProperty("user.name"));
    assertEquals(job.getTtlSecondsAfterFinishing(), DEFAULT_CLEANUP_TTL_SECONDS);
  }
}
