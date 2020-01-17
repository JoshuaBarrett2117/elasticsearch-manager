package com.code.ssh;

import java.io.File;

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

    void sendFile(File file);

    File readFile(String path);
}
