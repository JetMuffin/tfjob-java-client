package org.kubeflow.client.examples;

import java.util.List;
import org.kubeflow.client.KubeflowClient;
import org.kubeflow.client.KubeflowClientFactory;
import org.kubeflow.client.model.Job;
import org.kubeflow.client.model.TFReplica;

public class Example {
  public static void main(String[] args) {
    try {
      KubeflowClient client =
          KubeflowClientFactory.newInstanceFromConfig("/home/mofeng.cj/kubeconfig");

      TFReplica ps =
          new TFReplica()
              .replicas(1)
              .cpu(1.0)
              .memory(1024.0)
              .image(
                  "reg.docker.alibaba-inc.com/pai-tensorflow/tensorflow-build:1.4.0-PAI1805u1-D1530175766-cpu")
              .env("HADOOP_HDFS_HOME", "/usr/local/hadoop")
              .env("RESOURCE_PATH", "hdfs://100.81.153.173:8020/tmp/a.tar.gz")
              .env("ENTRY_FILE", "mnist_hdfs.py")
              .args(
                  "--train_tfrecords=hdfs://100.81.153.173:8020/tmp/train.tfrecords --test_tfrecords=hdfs://100.81.153.173:8020/tmp/test.tfrecords");
      TFReplica worker =
          new TFReplica()
              .replicas(1)
              .cpu(2.0)
              .memory(2048.0)
              .image(
                  "reg.docker.alibaba-inc.com/pai-tensorflow/tensorflow-build:1.4.0-PAI1805u1-D1530175766-cpu")
              .env("HADOOP_HDFS_HOME", "/usr/local/hadoop")
              .env("RESOURCE_PATH", "hdfs://100.81.153.173:8020/tmp/a.tar.gz")
              .env("ENTRY_FILE", "mnist_hdfs.py")
              .args(
                  "--train_tfrecords=hdfs://100.81.153.173:8020/tmp/train.tfrecords --test_tfrecords=hdfs://100.81.153.173:8020/tmp/test.tfrecords");
      Job job = new Job().name("sdk-job").ps(ps).worker(worker).cleanupPolicy("running");
      client.submitJob(job);

      List<Job> jobs = client.listJobs();
      for (Job j : jobs) {
        System.out.println(j);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
