package com.yunji.titanrtx.agent;

import com.yunji.titanrtx.plugin.http.AHCBuildTool;
import com.yunji.titanrtx.plugin.http.HttpBuildTool;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLException;
import java.util.concurrent.CompletableFuture;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;

/**
 * @author Denim.leihz 2019-11-28 5:35 PM
 */
public class AHCClientTest {

    private AsyncHttpClient asyncHttpClient;

    private SslContext sslContext = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

    public AHCClientTest() throws SSLException {
    }


    @Before
    public void before() {
        asyncHttpClient = Dsl.asyncHttpClient(
                Dsl.config()
                        .setMaxConnections(AHCBuildTool.ASYNC_MAX_CONNECT_NUM)
                        .setSslContext(sslContext)
                        .setKeepAlive(true)
                        .setConnectTimeout(HttpBuildTool.CONNECT_TIMEOUT)
                        .setRequestTimeout(HttpBuildTool.REQUEST_TIMEOUT)
        );
    }

    @Test
    public void test() throws InterruptedException {
        while (true) {
            Request request = Dsl.get("https://fdfs.yunjiweidian.com/1ms")
//            Request request = Dsl.get("http://www.hzways.com")

                    .setHeader(CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                    .build();


            CompletableFuture<Response> responseFuture = asyncHttpClient
                    .executeRequest(request)
                    .toCompletableFuture();

            responseFuture.whenComplete((result, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                }
                System.out.println(result);
            });

            Thread.sleep(10000);
        }

    }
}
