package com.code.ssh.ssh2;

import ch.ethz.ssh2.*;
import com.code.common.util.StringUtils;
import com.code.common.util.TransferUtils;
import com.code.ssh.ISSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * @Description
 * @Author liufei
 * @Date 2020/1/14 15:58
 */
public class SSH2Client implements ISSHClient {
    private static final Logger logger = LoggerFactory.getLogger(SSH2Client.class);
    private static final String DEFAULT_CHARTSET = "UTF-8";
    private Connection conn;

    public SSH2Client(String ip, int port, String userName, String passWord) {
        try {
            conn = new Connection(ip, port);
            conn.connect();
            conn.authenticateWithPassword(userName, passWord);
            conn.addConnectionMonitor((throwable) -> {
                throw new RuntimeException("连接丢失,请重新创建连接", throwable);
            });
            logger.info("成功创建ssh连接");
        } catch (IOException e) {
            throw new RuntimeException("创建ssh连接失败", e);
        }
    }


    @Override
    public String sendCmd(String cmd) {
        String result = "";
        Session session = null;
        try {
            session = conn.openSession();
            session.execCommand(cmd);
            result = processStdout(session.getStdout(), DEFAULT_CHARTSET);
        } catch (IOException e) {
            logger.error("执行出错[{}]", cmd, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return result;
    }

    @Override
    public boolean isEmptyPath(String path) {
        String temp = sendCmd("ls " + path);
        if (StringUtils.isNotBlank(temp)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean sendFile(String remoteFilePath, byte[] file) {
        int i = remoteFilePath.lastIndexOf("/");
        String fieldName = null;
        String dir = null;
        if (i < 0) {
            throw new RuntimeException("路径不正确");
        } else {
            dir = remoteFilePath.substring(0, i + 1);
            fieldName = remoteFilePath.substring(i + 1, remoteFilePath.length());
        }
        boolean bool = false;
        SCPClient scpClient = null;
        try {
            scpClient = conn.createSCPClient();
            SCPOutputStream outputStream = scpClient.put(fieldName, file.length, dir, null);
            outputStream.write(file);
            outputStream.flush();
            outputStream.close();
            bool = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            bool = false;
        }
        return bool;
    }

    @Override
    public byte[] readFile(String path) {
        boolean bool = false;
        byte[] result = null;
        try {
            SCPClient scpClient = conn.createSCPClient();
            SCPInputStream scpInputStream = scpClient.get(path);
            result = input2Bytes(scpInputStream);
            bool = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            bool = false;
        }
        return result;
    }

    @Override
    public void close() {
        conn.close();
    }

    public byte[] input2Bytes(InputStream inputStream) {
        byte[] buffer = null;
        ByteArrayOutputStream bos = null;

        try {
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = inputStream.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException ex) {
            } finally {
                try {
                    if (null != inputStream) {
                        inputStream.close();
                    }
                } catch (IOException ex) {
                }
            }
        }
        return buffer;
    }

    /**
     * @param in      输入流对象
     * @param charset 编码
     * @return String 以纯文本的格式返回
     * @throws
     * @Title: processStdout
     * @Description: 解析脚本执行的返回结果
     */
    public String processStdout(InputStream in, String charset) {
        InputStream stdout = new StreamGobbler(in);
        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout, charset));
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line + "\n");
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("解析脚本执行行返回结果失败", e);
        } catch (IOException e) {
            logger.error("解析脚本执行行返回结果失败", e);
        }
        return buffer.toString();
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
        SSH2Client ssh2Client = new SSH2Client("192.168.123.25", 22, "es", "123456");
        String installPath = "/home/es/elasticsearch7";
        String temp = ssh2Client.sendCmd("ls " + installPath);
        if (StringUtils.isNotBlank(temp)) {
            throw new RuntimeException("[" + installPath + "]不是一个空目录");
        }
        String mkResult = ssh2Client.sendCmd("mkdir " + installPath);
        String s1 = ssh2Client.sendCmd("tar -zxvf /home/es/elasticsearch-7.5.1-linux-x86_64.tar.gz -C " + installPath);
        if (StringUtils.isBlank(s1)) {
            throw new RuntimeException("解压文件失败");
        }
        String sonPath = ssh2Client.sendCmd("ls " + installPath);
        ssh2Client.sendCmd("mv " + installPath + "/" + sonPath.trim() + "/* " + installPath);
        ssh2Client.sendCmd("rm " + installPath + "/" + sonPath.trim());

        String s = ssh2Client.sendCmd("cat " + installPath + "/config/elasticsearch.yml");
        Properties properties = TransferUtils.yml2Properties(s);
        properties.put("node.name", "es-master1");
        properties.put("node.master", "true");
        properties.put("node.data", "true");
        properties.put("path.data", "/home/es/es-7-data");
        properties.put("path.logs", "/home/es/es-7-logs");
        properties.put("cluster.name", "es-7-5-1-1");
        properties.put("cluster.initial_master_nodes", "es-master1");
        properties.put("network.host", "0.0.0.0");
        properties.put("http.port", "9204");
        byte[] bytes = TransferUtils.properties2Yaml(properties);
        boolean b = ssh2Client.sendFile(installPath + "/config/elasticsearch.yml", bytes);
        ssh2Client.conn.close();
        System.out.println(b);
    }
}
