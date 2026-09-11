/**
 * ArgoFeignConfig.java
 *
 * @author AC
 * @date 06-May-2025
 */
package com.nnp.envrep.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nnp.envrep.exception.ArgoClientErrorDecoder;

import feign.Client;
import feign.Logger;
import feign.codec.ErrorDecoder;
import io.netty.channel.DefaultAddressedEnvelope;
import lombok.extern.slf4j.Slf4j;

/**
 * ArgoFeignConfig.java
 *
 * @author AC
 * @date 06-May-2025
 */
@Configuration
@Slf4j
public class ArgoFeignConfig {
	

    @Bean
    public Client feignClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(
                                X509Certificate[] certs, String authType) {
                            // Intentionally empty
                        }

                        @Override
                        public void checkServerTrusted(
                                X509Certificate[] certs, String authType) {
                            // Intentionally empty
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            return new Client.Default(
                    socketFactory,
                    (hostname, session) -> true
            );

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(
                    "Failed to initialize Feign SSL client", e);
        }
    }


    @Bean
    public ErrorDecoder errorDecoder() {
        return new ArgoClientErrorDecoder();
    }
    
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

}
