package org.kubeflow.client.storage;

import static org.kubeflow.client.model.JobConstants.DEFAULT_REMOTE_ROOT_DIR;
import static org.kubeflow.client.storage.HDFSConstants.HDFS_IPC_CLIENT_CONNECT_MAX_RETRIES;
import static org.kubeflow.client.storage.HDFSConstants.HDFS_IPC_CLIENT_CONNECT_TIMEOUT;
import static org.kubeflow.client.storage.HDFSConstants.HFDS_IPC_CLIENT_CONNECT_RETRY_INTERVAL;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSStorage implements Storage {
  private Configuration conf;

  private String resourceRootDir = DEFAULT_REMOTE_ROOT_DIR;

  public HDFSStorage() {
    this.conf = new Configuration();
  }

  public HDFSStorage defaultFS(String defaultFS) {
    this.conf.set("fs.defaultFS", defaultFS);
    this.conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
    this.conf.set("ipc.client.connect.max.retries", Integer.toString(HDFS_IPC_CLIENT_CONNECT_MAX_RETRIES));
    this.conf.set("ipc.client.connect.timeout", Integer.toString(HDFS_IPC_CLIENT_CONNECT_TIMEOUT));
    this.conf.set("ipc.client.connect.retry.interval", Integer.toString(HFDS_IPC_CLIENT_CONNECT_RETRY_INTERVAL));
    return this;
  }

  public String getDefaultFS(String defaultFS) {
    return this.conf.get("fs.defaultFS");
  }

  public HDFSStorage addConfig(String key, String value) {
      this.conf.set(key, value);
      return this;
  }

  public String getConfig(String key) {
      return this.conf.get(key);
  }

  public HDFSStorage resourceRootDir(String resourceRootDir) {
    this.resourceRootDir = resourceRootDir;
    return this;
  }

  public String getResourceRootDir() {
    return this.resourceRootDir;
  }

  public String getScheme() {
    URI uri = URI.create(this.conf.get("fs.defaultFS"));
    return uri.getScheme();
  }

  public String getAddress() {
    URI uri = URI.create(this.conf.get("fs.defaultFS"));
    return uri.toString();
  }

  public String getHost() {
    URI uri = URI.create(this.conf.get("fs.defaultFS"));
    return uri.getHost();
  }

  public int getPort() {
    URI uri = URI.create(this.conf.get("fs.defaultFS"));
    return uri.getPort();
  }

  @Override
  public void upload(String src, String dest) throws IOException {
    FileSystem fs = FileSystem.get(this.conf);
    Path localPath = new Path(src);
    FileInputStream fileInputStream = new FileInputStream(localPath.toString());

    Path remotePath = new Path(dest);
    if (!fs.exists(remotePath.getParent())) {
      fs.mkdirs(remotePath.getParent());
    }

    FSDataOutputStream fsDataOutputStream = fs.create(remotePath);
    IOUtils.copy(fileInputStream, fsDataOutputStream);

    fsDataOutputStream.close();
    fileInputStream.close();
  }
}
