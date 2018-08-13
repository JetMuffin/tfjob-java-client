package org.kubeflow.client.model;

import static org.kubeflow.client.model.JobConstants.*;

import io.kubernetes.client.models.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.kubeflow.client.models.V1alpha2TFJob;
import org.kubeflow.client.models.V1alpha2TFJobSpec;
import org.kubeflow.client.util.JobUtil;

public class Job {
  private V1alpha2TFJob tfjob;

  private String script;
  private String remoteScript;
  private String uuid = JobUtil.generateUUID();

  public Job() {
    this.tfjob =
        new V1alpha2TFJob()
            .apiVersion(JobConstants.KUBEFLOW_API_VERSION)
            .kind(JobConstants.KUBEFLOW_JOB_KIND)
            .spec(new V1alpha2TFJobSpec().ttlSecondsAfterFinishing(DEFAULT_CLEANUP_TTL_SECONDS))
            .metadata(new V1ObjectMeta());
  }

  public Job(V1alpha2TFJob tfjob) {
    this.tfjob = tfjob;
  }

  public void setTfjob(V1alpha2TFJob tfjob) {
    this.tfjob = tfjob;
  }

  public Job user(String user) {
    this.tfjob.getMetadata().putLabelsItem("user", user);
    return this;
  }

  public String getUser() {
    Map<String, String> labels = this.tfjob.getMetadata().getLabels();
    if (labels == null) {
      return null;
    }
    if (labels.containsKey(KUBEFLOW_LABEL_USER)) {
      return labels.get(KUBEFLOW_LABEL_USER);
    }
    return null;
  }

  public String getUUID() {
    return this.uuid;
  }

  public Job name(String name) {
    this.tfjob.getMetadata().name(name);
    return this;
  }

  public String getName() {
    return this.tfjob.getMetadata().getName();
  }

  public Job generateName(String generateName) {
    this.tfjob.getMetadata().generateName(generateName);
    return this;
  }

  public String getGenerateName(String generateName) {
    return this.tfjob.getMetadata().getGenerateName();
  }

  public Job namespace(String namespace) {
    this.tfjob.getMetadata().namespace(namespace);
    return this;
  }

  public String getNamespace() {
    return this.tfjob.getMetadata().getNamespace();
  }

  public Job script(String script) {
    this.script = script;
    return this;
  }

  public String getScript() {
    return this.script;
  }

  public Job remoteScript(String remoteScript) {
    this.remoteScript = remoteScript;
    return this;
  }

  public String getRemoteScript() {
    return this.remoteScript;
  }

  public Job cleanupPolicy(String cleanupPolicy) {
    this.tfjob.getSpec().cleanPodPolicy(cleanupPolicy);
    return this;
  }

  public String getCleanupPolicy() {
    return this.tfjob.getSpec().getCleanPodPolicy();
  }

  public Job ttlSecondsAfterFinishing(int ttl) {
    this.tfjob.getSpec().ttlSecondsAfterFinishing(ttl);
    return this;
  }

  public int getTtlSecondsAfterFinishing() {
    return this.tfjob.getSpec().getTtlSecondsAfterFinishing();
  }

  public Job ps(TFReplica replica) {
    replica.priority(DEFAULT_PS_PRIORITY);
    this.tfjob
        .getSpec()
        .putTfReplicaSpecsItem(JobConstants.KUBEFLOW_PS_REPLICA_NAME, replica.getSpec());
    return this;
  }

  public TFReplica getPs() {
    if (!this.tfjob
        .getSpec()
        .getTfReplicaSpecs()
        .containsKey(JobConstants.KUBEFLOW_PS_REPLICA_NAME)) {
      return null;
    }
    return new TFReplica(
        this.tfjob.getSpec().getTfReplicaSpecs().get(JobConstants.KUBEFLOW_PS_REPLICA_NAME));
  }

  public Job worker(TFReplica replica) {
    this.tfjob
        .getSpec()
        .putTfReplicaSpecsItem(JobConstants.KUBEFLOW_WORKER_REPLICA_NAME, replica.getSpec());
    return this;
  }

  public TFReplica getWorker() {
    if (!this.tfjob
        .getSpec()
        .getTfReplicaSpecs()
        .containsKey(JobConstants.KUBEFLOW_WORKER_REPLICA_NAME)) {
      return null;
    }
    return new TFReplica(
        this.tfjob.getSpec().getTfReplicaSpecs().get(JobConstants.KUBEFLOW_WORKER_REPLICA_NAME));
  }

  /**
   * Path to access script in remote storage backend in the form of
   * `/<prefix>/<user>/<namespace>/tfjob/<uuid>/`
   *
   * @return this path
   */
  public String getRemoteScriptPath(String prefix) {
    if (this.getUser() == null
        || this.getNamespace() == null
        || this.getScript() == null
        || this.getUUID() == null) {
      return null;
    }
    String file = Paths.get(this.getScript()).getFileName().toString();

    Path remotePath =
        Paths.get(
            prefix,
            this.getUser(),
            this.getNamespace(),
            KUBEFLOW_JOB_KIND.toLowerCase(),
            this.getUUID(),
            file);

    // Paths.get reads `path.separator` from system property, and use backslash rather than
    // slash if run on Windows, so we restrict the separator here.
    return remotePath.toString().replace("\\", "/");
  }

  public V1alpha2TFJob getTfjob() {
    return this.tfjob;
  }
}
