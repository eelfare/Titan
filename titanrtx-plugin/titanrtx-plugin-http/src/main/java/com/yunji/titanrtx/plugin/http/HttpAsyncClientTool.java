package com.yunji.titanrtx.plugin.http;

import org.apache.http.Consts;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.*;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.nio.charset.CodingErrorAction;

/**
 * HTTP Async Client helper
 *
 *
 * optimized for pressure
 */
public class HttpAsyncClientTool {


    public static CloseableHttpAsyncClient createAsyncClient() {
        return createAsyncClient(HttpBuildTool.CONNECT_TIMEOUT);
    }

    public static CloseableHttpAsyncClient createAsyncClient(int connectTimeOut) {
        return createAsyncClient(connectTimeOut,HttpBuildTool.SOCKET_TIMEOUT);
    }

    public static CloseableHttpAsyncClient createAsyncClient(int connectTimeOut,int socketTimeOut) {
        return createAsyncClient(connectTimeOut,socketTimeOut,HttpBuildTool.MAX_CONNECT_NUM);
    }

    public static CloseableHttpAsyncClient createAsyncClient(int connectTimeOut,int socketTimeOut,int maxConnectNum)  {
        return createAsyncClient(connectTimeOut,socketTimeOut,maxConnectNum,HttpBuildTool.MAX_PER_ROUTE);
    }

    public static CloseableHttpAsyncClient createAsyncClient(int connectTimeOut,int socketTimeOut,int maxConnect,int maxPerRoute) {

        SSLContext sslcontext = SSLContexts.createDefault();

        Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder
                .<SchemeIOSessionStrategy> create()
                .register("http", NoopIOSessionStrategy.INSTANCE)
                .register("https", new SSLIOSessionStrategy(sslcontext))
                .build();


        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                .build();

        ConnectingIOReactor ioReactor = null;
        try {
            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (IOReactorException e) {
            e.printStackTrace();
        }

        PoolingNHttpClientConnectionManager conMgr = new PoolingNHttpClientConnectionManager(ioReactor, sessionStrategyRegistry);
        conMgr.setMaxTotal(maxConnect);
        conMgr.setDefaultMaxPerRoute(maxPerRoute);

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8).build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeOut)
                .setSocketTimeout(socketTimeOut)
                .build();

        Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder
                .<AuthSchemeProvider> create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                .register(AuthSchemes.DIGEST, new DigestSchemeFactory())
                .register(AuthSchemes.NTLM, new NTLMSchemeFactory())
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())
                .build();
        conMgr.setDefaultConnectionConfig(connectionConfig);

        return HttpAsyncClients.custom().setConnectionManager(conMgr)
                .setDefaultAuthSchemeRegistry(authSchemeRegistry)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(new BasicCookieStore()).build();

    }


}
