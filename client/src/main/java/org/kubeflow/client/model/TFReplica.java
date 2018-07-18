package org.kubeflow.client.model;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import java.util.*;
import org.kubeflow.client.models.V1alpha2TFReplicaSpec;
import org.kubeflow.client.util.JobUtil;

public class TFReplica {
  private V1alpha2TFReplicaSpec spec;

  public TFReplica() {
    V1PodSpec podSpec = new V1PodSpec();
    V1Container container = new V1Container().name(JobConstants.KUBEFLOW_CONTAINER_NAME);
    container.resources(new V1ResourceRequirements());
    podSpec.addContainersItem(container);
    this.spec = new V1alpha2TFReplicaSpec().template(new V1PodTemplateSpec().spec(podSpec));
  }

  public TFReplica(V1alpha2TFReplicaSpec spec) {
    this.spec = spec;
  }

  public TFReplica image(String image) {
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        container.setImage(image);
      }
    }
    return this;
  }

  public String getImage() {
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        return container.getImage();
      }
    }
    return null;
  }

  public TFReplica replicas(int replica) {
    this.spec.setReplicas(replica);
    return this;
  }

  public int getReplicas() {
    return this.spec.getReplicas();
  }

  public TFReplica command(String command) {
    if (command != null && command.length() > 0) {
      String[] commands = command.split("\\s+");
      for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
        container.setCommand(Arrays.asList(commands));
      }
    }
    return this;
  }

  public TFReplica env(String name, String value) {
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        container.addEnvItem(new V1EnvVar().name(name).value(value));
      }
    }
    return this;
  }

  public Map<String, String> getEnvs() {
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        Map<String, String> envs = new HashMap<>();
        for (V1EnvVar item : container.getEnv()) {
          envs.put(item.getName(), item.getValue());
        }
        return envs;
      }
    }
    return null;
  }

  public String getCommand() {
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        return JobUtil.joinStrings(container.getCommand());
      }
    }
    return null;
  }

  public TFReplica cpu(Double cpu) {
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        container
            .getResources()
            .putRequestsItem(
                JobConstants.KUBEFLOW_RESOURCE_CPU, new Quantity(Double.toString(cpu)));
      }
    }
    return this;
  }

  public Double getCpu() {
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        return container
            .getResources()
            .getRequests()
            .get(JobConstants.KUBEFLOW_RESOURCE_CPU)
            .getNumber()
            .toBigInteger()
            .doubleValue();
      }
    }
    return 0.0;
  }

  public TFReplica memory(Double memory) {
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        container
            .getResources()
            .putRequestsItem(
                JobConstants.KUBEFLOW_RESOURCE_MEMORY, new Quantity(Double.toString(memory)));
      }
    }
    return this;
  }

  public Double getMemory() {
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        return container
            .getResources()
            .getRequests()
            .get(JobConstants.KUBEFLOW_RESOURCE_MEMORY)
            .getNumber()
            .toBigInteger()
            .doubleValue();
      }
    }
    return 0.0;
  }

  public TFReplica args(String args) {
    String[] argSplit = args.split("\\s+");
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        container.setArgs(Arrays.asList(argSplit));
      }
    }
    return this;
  }

  public String getArgs() {
    for (V1Container container : this.spec.getTemplate().getSpec().getContainers()) {
      if (container.getName().equals(JobConstants.KUBEFLOW_CONTAINER_NAME)) {
        return JobUtil.joinStrings(container.getArgs());
      }
    }
    return null;
  }

  public V1alpha2TFReplicaSpec getSpec() {
    return this.spec;
  }
}
