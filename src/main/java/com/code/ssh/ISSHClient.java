package com.code.ssh;

/**
 * @Description SSHClient
 * @Author liufei
 * @Date 2020/1/14 14:34
 */
public interface ISSHClient {
    /**
     * 发送指令
     *
     * @param cmd 指令
     * @return 返回结果
     */
    String sendCmd(String cmd);

    boolean  isEmptyPath(String path);

    boolean sendFile(String remoteFilePath, byte[] file);

    byte[] readFile(String path);

    void close();
}
