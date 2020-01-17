package com.code.ssh.ssh2;

import ch.ethz.ssh2.*;
import com.code.common.StringUtils;
import com.code.common.TransferUtils;
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
    public void sendFile(File file) {
        try {
            SFTPv3Client client = new SFTPv3Client(conn);
            SFTPv3FileHandle file1 = client.createFile(file.getName());
            SFTPv3Client client1 = file1.getClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File readFile(String path) {
        return null;
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


    public static void main(String[] args) {
        SSH2Client ssh2Client = new SSH2Client("192.168.123.25", 22, "es", "123456");
        String yml = ssh2Client.sendCmd("cat /home/es/elasticsearch-7.5.1/config/elasticsearch.yml");


        Properties properties = TransferUtils.yml2Properties(yml);
        System.out.println(1);
    }
}
