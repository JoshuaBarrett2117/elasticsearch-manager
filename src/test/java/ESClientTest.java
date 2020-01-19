import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.Stats;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @Description
 * @Date 2020/1/19 17:06
 * @Created by liufei
 */
public class ESClientTest {
    private JestClient jestClient;

    @Before
    public void init() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(
                new HttpClientConfig.Builder(Arrays.asList("http://192.168.123.25:9204"))
                        .defaultMaxTotalConnectionPerRoute(32)
                        .connTimeout(60000)
                        .readTimeout(60000)
                        .maxTotalConnection(100)
                        .multiThreaded(true)
                        .build()
        );
        jestClient = factory.getObject();
    }

    @Test
    public void getNodesTest() {
        try {
            JestResult execute = jestClient.execute(
                    new Stats.Builder()
                            .addIndex("test1")
                            .build());
            System.out.println(execute.getJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
