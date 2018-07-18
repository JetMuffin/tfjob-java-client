# tfjob-java-client
Java Client for Kubeflow TFJob

## Requirements

Building the API client library requires [Maven](https://maven.apache.org/) to be installed.

## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn install -DskipTests
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn deploy
```

Refer to the [official documentation](https://maven.apache.org/plugins/maven-deploy-plugin/usage.html) for more information.

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
    <groupId>org.kubeflow.client</groupId>
    <artifactId>client-java</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

## Getting Started

Please refer to the [example](examples/src/main/java/org/kubeflow/client/examples/Example.java) codes.

```java
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
```