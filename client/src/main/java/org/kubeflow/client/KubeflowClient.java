package org.kubeflow.client;

import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1Status;
import java.util.ArrayList;
import java.util.List;
import org.kubeflow.client.apis.KubeflowOrgV1alpha2Api;
import org.kubeflow.client.exception.ClientException;
import org.kubeflow.client.model.Job;
import org.kubeflow.client.model.JobConstants;
import org.kubeflow.client.models.V1alpha2TFJob;
import org.kubeflow.client.models.V1alpha2TFJobList;

public class KubeflowClient {
  private KubeflowOrgV1alpha2Api api;
  private String defaultNamespace = JobConstants.DEFAULT_NAMESPACE;

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

  private boolean validateJob(Job job) {
    if (job.getName() == null || job.getNamespace() == null) {
      return false;
    }
    if (job.getScript() == null) {
      return false;
    }
    if (job.getPs() == null || job.getWorker() == null) {
      return false;
    }
    if (job.getPs().getReplicas() <= 0 || job.getWorker().getReplicas() <= 0) {
      return false;
    }
    return true;
  }

  /**
   * create a job
   *
   * @param job (required)
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   *     response body
   */
  public void submitJob(Job job) throws ClientException {
    submitJob(job, this.defaultNamespace);
  }

  /**
   * create a job
   *
   * @param job (required)
   * @param namespace object name and auth scope, such as for teams and projects
   * @throws ClientException If fail to call the API, e.g. server error or cannot deserialize the
   *     response body
   */
  public void submitJob(Job job, String namespace) throws ClientException {
    if (!validateJob(job)) {
      throw new ClientException("Invalid job: missing required fileds.");
    }
    V1alpha2TFJob tfjob = job.getTfjob();
    try {
      job.setTfjob(this.api.createNamespacedTFJob(namespace, tfjob, "true"));
    } catch (ApiException e) {
      throw new ClientException("Cannot connect to kubernetes api: " + e.getMessage());
    }
  }

  /**
   * list all jobs with default parameters
   *
   * @return list of job
   * @throws ClientException
   */
  public List<Job> listJobs() throws ClientException {
    return listJobs(this.defaultNamespace);
  }

  /**
   * list all jobs with given namespace
   *
   * @param namespace object name and auth scope, such as for teams and projects (optional, default
   *     to 'default')
   * @return list of job
   * @throws ClientException
   */
  public List<Job> listJobs(String namespace) throws ClientException {
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
   * @throws ClientException
   */
  public List<Job> listJobs(
      String namespace, String labelSelector, Integer limit, Boolean includeUninitialized)
      throws ClientException {
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
      throw new ClientException("Cannot connect to kubernetes api: " + e.getMessage());
    }
  }

  /**
   * delete a job
   *
   * @param name name of job
   * @return success or not
   * @throws ApiException
   */
  public void deleteJob(String name) throws ClientException {
    deleteJob(name, this.defaultNamespace);
  }

  /**
   * delete a job
   *
   * @param name name of this job
   * @param namespace namespace of this job
   * @return success or not
   * @throws ClientException
   */
  public void deleteJob(String name, String namespace) throws ClientException {
    try {
      V1DeleteOptions options = new V1DeleteOptions();
      V1Status status =
          this.api.deleteNamespacedTFJob(name, namespace, options, null, null, null, null);
      if (!status.getStatus().equals("success")) {
        throw new ClientException("Failed to delete job: " + status.getMessage());
      }
    } catch (ApiException e) {
      throw new ClientException("Cannot connect to kubernetes api: " + e.getMessage());
    }
  }

  /**
   * update a job
   *
   * @param name name of this job
   * @param job new configuration of this job
   * @throws ClientException
   */
  public void updateJob(String name, Job job) throws ClientException {
    updateJob(name, this.defaultNamespace, job);
  }

  /**
   * update a job
   *
   * @param name name of this job
   * @param namespace namespace of this job
   * @param job new configuration of this job
   * @throws ClientException
   */
  public void updateJob(String name, String namespace, Job job) throws ClientException {
    try {
      job.setTfjob(this.api.patchNamespacedTFJob(name, namespace, job, null));
    } catch (ApiException e) {
      throw new ClientException("Cannot connect to kubernetes api: " + e.getMessage());
    }
  }
}
