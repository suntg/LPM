// package com.example.lpm.config;
//
// import java.io.IOException;
// import java.net.InetSocketAddress;
// import java.net.Proxy;
// import java.util.concurrent.TimeUnit;
//
// import javax.annotation.Resource;
//
// import org.jetbrains.annotations.NotNull;
// import org.jetbrains.annotations.Nullable;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// import com.ejlchina.okhttps.HTTP;
//
// import okhttp3.*;
//
// @Configuration
// public class OkHttpConfig {
//
// @Resource
// private LuminatiProperties luminatiProperties;
//
// @Bean
// public HTTP http() {
// return HTTP.builder().config((OkHttpClient.Builder builder) -> {
// // 配置连接池 最小10个连接（不配置默认为 5）
// builder.connectTimeout(10, TimeUnit.SECONDS);
// builder.readTimeout(20, TimeUnit.SECONDS);
// builder.writeTimeout(10, TimeUnit.SECONDS);
// builder.connectionPool(new ConnectionPool(100, 5, TimeUnit.MINUTES));
// // 配置 Proxy
// builder.proxy(new Proxy(Proxy.Type.HTTP,
// new InetSocketAddress(luminatiProperties.getProxyHost(), luminatiProperties.getProxyPort())));
// builder.proxyAuthenticator(new Authenticator() {
// // 设置代理服务器账号密码
// final String credential =
// Credentials.basic(luminatiProperties.getProxyUsername(), luminatiProperties.getProxyPassword());
//
// @Nullable
// @Override
// public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
// return response.request().newBuilder().header("Proxy-Authorization", credential)
// .header("Content-Encoding", "gzip").build();
// }
// });
// }).build();
// }
// }
