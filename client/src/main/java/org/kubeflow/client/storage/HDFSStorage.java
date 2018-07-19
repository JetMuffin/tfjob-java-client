package org.kubeflow.client.storage;

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

  public HDFSStorage() {
    this.conf = new Configuration();
  }

  public HDFSStorage(String defaultFS) {
    this.conf = new Configuration();
    this.conf.set("fs.defaultFS", defaultFS);
    this.conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
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
    FileInputStream fileInputStream = new FileInputStream(localPath.getName());

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
