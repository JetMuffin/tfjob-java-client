package org.kubeflow.client;

import static org.kubeflow.client.model.JobConstants.KUBEFLOW_RESOURCE_PATH_ENV;

import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.kubeflow.client.apis.KubeflowOrgV1alpha2Api;
import org.kubeflow.client.exception.KubeflowException;
import org.kubeflow.client.model.Job;
import org.kubeflow.client.model.JobConstants;
import org.kubeflow.client.models.V1alpha2TFJob;
import org.kubeflow.client.models.V1alpha2TFJobList;
import org.kubeflow.client.storage.Storage;

public class KubeflowClient {
  private String defaultNamespace = JobConstants.DEFAULT_NAMESPACE;

  private KubeflowOrgV1alpha2Api api;
  private Storage storage;

  KubeflowClient(KubeflowOrgV1alpha2Api api) {
    this.api = api;
  }

  KubeflowClient(KubeflowOrgV1alpha2Api api, String defaultNamespace) {
    this.api = api;
    this.defaultNamespace = defaultNamespace;
  }

  public String getDefaultNamespace() {
    return this.defaultNamespace;
  }

  private boolean validateJob(Job job) throws KubeflowException {
    if (job.getName() == null) {
      throw new KubeflowException("Invalid job: missing 'name' field.");
    }
    if (job.getScript() == null) {
      throw new KubeflowException("Invalid job: missing 'script' field.");
    }
    if (job.getPs() == null || job.getPs().getReplicas() <= 0) {
      throw new KubeflowException("Must specify at least one PS.");
    }
    if (job.getWorker() == null || job.getWorker().getReplicas() <= 0) {
      throw new KubeflowException("Must specify at least one Worker.");
    }

    return true;
  }

  public KubeflowClient defaultNamespace(String defaultNamespace) {
    this.defaultNamespace = defaultNamespace;
    return this;
  }

  public KubeflowClient storage(Storage storage) {
    this.storage = storage;
    return this;
  }

  /**
   * create a job
   *
   * @param job (required)
   * @throws KubeflowException If fail to call the API, e.g. server error or cannot deserialize the
   *     response body
   */
  public void submitJob(Job job) throws KubeflowException, IOException {
    submitJob(job, this.defaultNamespace);
  }

  /**
   * create a job
   *
   * @param job (required)
   * @param namespace object name and auth scope, such as for teams and projects
   * @throws KubeflowException If fail to call the API, e.g. server error or cannot deserialize the
   *     response body
   */
  public void submitJob(Job job, String namespace) throws KubeflowException, IOException {
    // validate this job
    if (validateJob(job)) {
      if (this.storage == null) {
        throw new KubeflowException("Need to specify a storage backend, e.g. hdfs.");
      }
      try {
        job.namespace(namespace);

        // submit script to remote storage backend
        String remoteScriptPath = job.getRemoteScriptPath();
        if (remoteScriptPath != null) {
          this.storage.upload(job.getScript(), remoteScriptPath);
        }

        // set `RESOURCE_PATH` env to TFReplicas
        String remoteScriptURI = this.storage.getAddress() + remoteScriptPath;
        job.getPs().env(KUBEFLOW_RESOURCE_PATH_ENV, remoteScriptURI);
        job.getWorker().env(KUBEFLOW_RESOURCE_PATH_ENV, remoteScriptURI);

        // submit job to kubeflow
        V1alpha2TFJob tfjob = job.getTfjob();
        job.setTfjob(this.api.createNamespacedTFJob(namespace, tfjob, "true"));
      } catch (ApiException e) {
        throw new KubeflowException("Cannot connect to kubernetes api: " + e.getMessage());
      }
    }
  }

  /**
   * list all jobs with default parameters
   *
   * @return list of job
   * @throws KubeflowException
   */
  public List<Job> listJobs() throws KubeflowException {
    return listJobs(this.defaultNamespace);
  }

  /**
   * list all jobs with given namespace
   *
   * @param namespace object name and auth scope, such as for teams and projects (optional, default
   *     to 'default')
   * @return list of job
   * @throws KubeflowException
   */
  public List<Job> listJobs(String namespace) throws KubeflowException {
    return listJobs(namespace, null, null, null);
  }

  /**
   * list all jobs with given requirements
   *
   * @param namespace object name and auth scope, such as for teams and projects (optional, default
   *     to 'default')
   * @param labelSelector A selector to restrict the list of returned objects by their labels.
   *     Defaults to everything. (optional)
   * @param limit limit is a maximum number of responses to return for a list call. (optional)
   * @param includeUninitialized If true, partially initialized resources are included in the
   *     response. (optional)
   * @return list of job
   * @throws KubeflowException
   */
  public List<Job> listJobs(
      String namespace, String labelSelector, Integer limit, Boolean includeUninitialized)
      throws KubeflowException {
    try {
      V1alpha2TFJobList list =
          this.api.listNamespacedTFJob(
              namespace,
              null,
              null,
              null,
              includeUninitialized,
              labelSelector,
              limit,
              null,
              null,
              null);
      List<Job> jobs = new ArrayList<Job>();

      for (V1alpha2TFJob tfjob : list.getItems()) {
        jobs.add(new Job(tfjob));
      }

      return jobs;
    } catch (ApiException e) {
      throw new KubeflowException("Cannot connect to kubernetes api: " + e.getMessage());
    }
  }

  /**
   * delete a job
   *
   * @param name name of job
   * @return success or not
   * @throws KubeflowException
   */
  public void deleteJob(String name) throws KubeflowException {
    deleteJob(name, this.defaultNamespace);
  }

  /**
   * delete a job
   *
   * @param name name of this job
   * @param namespace namespace of this job
   * @return success or not
   * @throws KubeflowException
   */
  public void deleteJob(String name, String namespace) throws KubeflowException {
    try {
      V1DeleteOptions options = new V1DeleteOptions();
      V1Status status =
          this.api.deleteNamespacedTFJob(name, namespace, options, null, null, null, null);
      if (!status.getStatus().equals("success")) {
        throw new KubeflowException("Failed to delete job: " + status.getMessage());
      }
    } catch (ApiException e) {
      throw new KubeflowException("Cannot connect to kubernetes api: " + e.getMessage());
    }
  }

  /**
   * update a job
   *
   * @param name name of this job
   * @param job new configuration of this job
   * @throws KubeflowException
   */
  public void updateJob(String name, Job job) throws KubeflowException {
    updateJob(name, this.defaultNamespace, job);
  }

  /**
   * update a job
   *
   * @param name name of this job
   * @param namespace namespace of this job
   * @param job new configuration of this job
   * @throws KubeflowException
   */
  public void updateJob(String name, String namespace, Job job) throws KubeflowException {
    try {
      job.setTfjob(this.api.patchNamespacedTFJob(name, namespace, job, null));
    } catch (ApiException e) {
      throw new KubeflowException("Cannot connect to kubernetes api: " + e.getMessage());
    }
  }
}
