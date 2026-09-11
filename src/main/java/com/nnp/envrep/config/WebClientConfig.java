/**
 * WebClientConfig.java
 *
 * @author AC
 * @date 29-Apr-2025
 */
package com.nnp.envrep.config;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * WebClientConfig.java
 *
 * @author AC
 * @date 29-Apr-2025
 */
@Configuration
@Slf4j
public class WebClientConfig {
	
    @Value("${redmine.service.baseurl:http://localhost:8095/redmineint}")
    private String redmineBaseUrl;
    
    @Value("${gitint.service.baseurl:http://localhost:8094/gitlabint}")
    private String gitIntServBaseUrl;

    @Value("${mail.service.baseurl:http://localhost:8080}")
    private String mailServiceBaseUrl;
    
    @Value("${haproxy.service.baseurl:http://localhost:8081}")
    private String haproxyServiceBaseUrl;
    
	
    @Bean(name = "webClientRedmineInt")
    public WebClient getRedmineWebClient() {
        return WebClient.builder()
                .baseUrl(redmineBaseUrl)
                .filter(logReq())
                //.filter(errorHandling())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(configWebClient()))
                .build();
    }

    @Bean(name = "webClientMailService")
    public WebClient getMailServiceWebClient() {
        return WebClient.builder()
                .baseUrl(mailServiceBaseUrl)
                .filter(logReq())
                //.filter(errorHandling())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(configWebClient()))
                .build();
    }
    
    @Bean(name = "webClientGitIntServ")
    public WebClient getGitIntWebClient() {
        return WebClient.builder()
                .baseUrl(gitIntServBaseUrl)
                .filter(logReq())
                //.filter(errorHandling())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(configWebClient()))
                .build();
    }
    
    @Bean(name = "webClientHAProxyInt")
    public WebClient getHAProxyIntWebClient() {
        return WebClient.builder()
                .baseUrl(haproxyServiceBaseUrl)
                .filter(logReq())
                //.filter(errorHandling())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(configWebClient()))
                .build();
    }
    

    private HttpClient configWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
                .responseTimeout(Duration.ofMillis(20000))
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(20000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(20000, TimeUnit.MILLISECONDS)));
        return httpClient;
    }

    private ExchangeFilterFunction logReq() {
        //            log.info("Logging request URL {}", clientRequest.url());
        return ExchangeFilterFunction.ofRequestProcessor(Mono::just);
    }
}
