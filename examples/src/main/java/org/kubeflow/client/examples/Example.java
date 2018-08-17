package org.kubeflow.client.examples;

import org.apache.commons.cli.*;
import org.kubeflow.client.KubeflowClient;
import org.kubeflow.client.KubeflowClientFactory;
import org.kubeflow.client.model.Job;
import org.kubeflow.client.model.TFReplica;
import org.kubeflow.client.storage.HDFSStorage;

public class Example {
  private static final String hdfsBackendURL = "hdfs://100.81.153.173:8020";
  private static final String panguURL = "10.182.6.58";
  private static final String resourceRootDir = "/tmp";
  private static final String baseImage =
      "reg.docker.alibaba-inc.com/pai-tensorflow/tensorflow-build:1.4.0-PAI1805u1-D1530175766-cpu";
  private static final String hadoopHome = "/usr/local/hadoop";
  private static final double defaultCpus = 1.5;
  private static final double defaultMemory = 3072.0;

  public static void main(String[] args) {
    try {
      CommandLineParser parser = new DefaultParser();
      Options options = new Options();
      options.addOption("n", "name", true, "");
      options.addOption("s", "script", true, "");
      options.addOption("f", "entry_file", true, "");
      options.addOption("p", "ps", true, "");
      options.addOption("w", "worker", true, "");
      options.addOption("a", "args", true, "");
      options.addOption("c", "cpu", true, "");
      options.addOption("m", "memory", true, "");
      options.addOption("k", "kubeconfig", true, "");
      options.addOption("h", "help", false, "");

      CommandLine commandLine = parser.parse(options, args);

      if (commandLine.hasOption("h")) {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("EXAMPLE example test", "", options, "");
        return;
      }

      KubeflowClient client;
      if (commandLine.hasOption("k")) {
        client =
            KubeflowClientFactory.newInstanceFromConfig(commandLine.getOptionValue("kubeconfig"));
      } else {
        client = KubeflowClientFactory.newInstance();
      }
      client.storage(new HDFSStorage().defaultFS(hdfsBackendURL).resourceRootDir(resourceRootDir));

      TFReplica ps =
          new TFReplica()
              .replicas(Integer.parseInt(commandLine.getOptionValue("ps")))
              .cpu(
                  Double.parseDouble(
                      commandLine.getOptionValue("cpu", String.valueOf(defaultCpus))))
              .memory(
                  Double.parseDouble(
                      commandLine.getOptionValue("memory", String.valueOf(defaultMemory))))
              .image(baseImage)
              .env("HADOOP_HDFS_HOME", hadoopHome)
              .env("ENTRY_FILE", commandLine.getOptionValue("entry_file"))
              .args(commandLine.getOptionValue("args"));

      TFReplica worker =
          new TFReplica()
              .replicas(Integer.parseInt(commandLine.getOptionValue("worker")))
              .cpu(
                  Double.parseDouble(
                      commandLine.getOptionValue("cpu", String.valueOf(defaultCpus))))
              .memory(
                  Double.parseDouble(
                      commandLine.getOptionValue("memory", String.valueOf(defaultMemory))))
              .image(baseImage)
              .env("HADOOP_HDFS_HOME", hadoopHome)
              .env("ENTRY_FILE", commandLine.getOptionValue("entry_file"))
              .args(commandLine.getOptionValue("args"));

      Job job =
          new Job()
              .user(System.getProperty("user.name"))
              .ps(ps)
              .worker(worker)
              .script(commandLine.getOptionValue("script"))
              .ttlSecondsAfterFinishing(60);

      client.submitJob(job);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
