package com.code.machine;

import com.code.common.util.Assert;
import com.code.common.util.StringUtils;
import com.code.common.util.TransferUtils;
import com.code.ssh.ISSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ElasticsearchMachine {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchMachine.class);
    private static final String DATA_PATH_KEY = "path.data";
    private static final String LOG_PATH_KEY = "path.logs";
    private ISSHClient client;
    private String installPath;
    private String esPid;

    public ElasticsearchMachine(ISSHClient client, String installPath) {
        this.client = client;
        this.installPath = installPath;
    }

    public void createES(String packagePath, Properties properties) {
        client.sendCmd("mkdir " + installPath);
        if (properties.get(DATA_PATH_KEY) != null) {
            logger.debug("创建数据目录[" + properties.get(DATA_PATH_KEY) + "]");
            client.sendCmd("mkdir " + properties.get(DATA_PATH_KEY));
        }
        if (properties.get(LOG_PATH_KEY) != null) {
            logger.debug("创建日志目录[" + properties.get(LOG_PATH_KEY) + "]");
            client.sendCmd("mkdir " + properties.get(LOG_PATH_KEY));
        }
        logger.debug("创建安装目录[" + installPath + "]");
        client.sendCmd("mkdir " + installPath);

        String s1 = client.sendCmd("tar -zxvf " + packagePath + " -C " + installPath);
        Assert.isTrue(StringUtils.isNotBlank(s1), "解压文件失败");
        String sonPath = client.sendCmd("ls " + installPath);
        Assert.isTrue(StringUtils.isNotBlank(s1), "解压文件失败");
        logger.debug("文件解压完成");
        client.sendCmd("mv " + installPath + "/" + sonPath.trim() + "/* " + installPath);
        client.sendCmd("rm -rf " + installPath + "/" + sonPath.trim());
        logger.debug("修改配置文件{}", properties);
        byte[] bytes = TransferUtils.properties2Yaml(properties);
        boolean b = client.sendFile(installPath + "/config/elasticsearch.yml", bytes);
        logger.debug("配置文件配置完成");
    }


    public void start() {
        client.sendCmd("sh " + installPath + "/bin/elasticsearch -d");
        logger.debug("正在启动ES进程");
        String s2 = client.sendCmd("ps -ef|grep " + installPath + " |grep java |grep -v grep");
        esPid = s2.split("(\\s)+")[1];
        logger.debug("当前elasticsearch进程号：{}", esPid);
    }

    public void stop() {
        logger.debug("当前elasticsearch进程号：{}", esPid);
        client.sendCmd("kill -9 " + esPid);
        logger.debug("当前elasticsearch已杀死：{}", esPid);
    }

    public Properties getESProperties() {
        String s = client.sendCmd("cat " + installPath + "/config/elasticsearch.yml");
        return TransferUtils.yml2Properties(s);
    }
}
