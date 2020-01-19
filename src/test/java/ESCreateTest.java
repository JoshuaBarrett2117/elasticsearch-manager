import com.code.common.util.Assert;
import com.code.machine.ElasticsearchMachine;
import com.code.ssh.ISSHClient;
import com.code.ssh.ssh2.SSH2Client;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class ESCreateTest {
    private ISSHClient client;

    @Before
    public void init() {
        client = new SSH2Client("192.168.123.25", 22, "es", "123456");
    }

    @Test
    public void esTest() {
        String installPath = "/home/es/elasticsearch7";
        validEmpty(installPath);
        String packagePath = "/home/es/elasticsearch-7.5.1-linux-x86_64.tar.gz";
        Properties properties = new Properties();
        properties.put("node.name", "es-master1");
        properties.put("node.master", "true");
        properties.put("node.data", "true");
        String dataPath = "/home/es/es-7-data1";
        validEmpty(dataPath);
        String logPath = "/home/es/es-7-logs1";
        validEmpty(logPath);
        properties.put("path.data", dataPath);
        properties.put("path.logs", logPath);
        properties.put("cluster.name", "es-7-5-1-1");
        properties.put("cluster.initial_master_nodes", "es-master1");
        properties.put("network.host", "0.0.0.0");
        properties.put("http.port", "9204");
        ElasticsearchMachine machine = new ElasticsearchMachine(client, installPath);
        machine.createES(packagePath, properties);
        machine.start();
        machine.stop();
    }

    private void validEmpty(String path) {
        Assert.isTrue(!client.isEmptyPath(path), "[" + path + "]不是一个空目录");
    }

    @Test
    public void deleteTestEs() {
        String s = client.sendCmd("ps -ef|grep /home/es/elasticsearch7 |grep java |grep -v grep");
        String[] split = s.split("(\\s)+");
        if (split.length > 1) {
            String esPid = split[1];
            client.sendCmd("kill -9 " + esPid);
        }
        client.sendCmd("rm -rf /home/es/elasticsearch7");
        client.sendCmd("rm -rf /home/es/es-7-data1");
        client.sendCmd("rm -rf /home/es/es-7-logs1");
        client.close();
    }

}
