package org.kubeflow.client.storage;

public class PanguStorage extends HDFSStorage {
  public PanguStorage() {
    super();
  }

  public PanguStorage defaultFS(String defaultFS) {
    super.conf.set("fs.defaultFS", defaultFS);
    super.conf.set("fs.pangu.impl", "com.alibaba.hdfs.fs.Pangu2FileSystem");
    super.conf.set(
        "fs.AbstractFileSystem.pangu.impl", "com.alibaba.hdfs.fs.Pangu2AbstractFileSystem");
    super.conf.set("fs.pangu.syncwrite", "true");
    super.conf.set("fs.pangu.writemode", "1");
    return this;
  }
}
