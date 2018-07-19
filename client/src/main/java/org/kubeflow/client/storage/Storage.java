package org.kubeflow.client.storage;

import java.io.IOException;

public interface Storage {

  /**
   * return the scheme of storage backend, e.g. hdfs://
   *
   * @return scheme
   */
  public String getScheme();

  /**
   * return full address(scheme://ip:port) of storage backend
   *
   * @return address
   */
  public String getAddress();

  /**
   * return ip of storage node
   *
   * @return ip adress
   */
  public String getHost();

  /**
   * return listen port of storage service
   *
   * @return port
   */
  public int getPort();

  /**
   * upload script from local to remote storage backend
   *
   * @param src local path of script
   * @param dest remote path to store uploaded script
   * @throws IOException
   */
  public void upload(String src, String dest) throws IOException;
}
