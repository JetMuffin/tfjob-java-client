package org.kubeflow.client.model;

public final class JobConstants {
  public static final String KUBEFLOW_API_VERSION = "kubeflow.org/v1alpha2";
  public static final String KUBEFLOW_JOB_KIND = "TFJob";
  public static final String KUBEFLOW_CONTAINER_NAME = "tensorflow";
  public static final String KUBEFLOW_PS_REPLICA_NAME = "PS";
  public static final String KUBEFLOW_WORKER_REPLICA_NAME = "Worker";
  public static final String KUBEFLOW_RESOURCE_CPU = "cpu";
  public static final String KUBEFLOW_RESOURCE_MEMORY = "memory";
  public static final String KUBEFLOW_RESOURCE_PATH_ENV = "RESOURCE_PATH";
  public static final String KUBEFLOW_LABEL_USER = "user";

  public static final String DEFAULT_NAMESPACE = "default";
  public static final String DEFAULT_SCRIPT_REMOTE_PATH_PREFIX = "/tmp";
  public static final String DEFAULT_GENERATE_NAME = "tfjob-";
}
