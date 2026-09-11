/**
 * SharedCompService.java
 *
 * @author AC
 * @date 13-Jun-2025
 */
package com.nnp.envrep.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.nnp.envrep.model.EnvReqComponent;
import com.nnp.envrep.model.Environment;
import com.nnp.envrep.model.UserV2;
import com.nnp.envrep.repo.UserRepoV2;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * SharedCompService.java
 *
 * @author AC
 * @date 13-Jun-2025
 */
@Service
@Slf4j
public class SharedCompService {

	@Autowired
	private UserRepoV2 userRepo;

	/**
	 * This method is a single point from where each shared component integration service will get invoked.
	 * Assumption is for each shared component like keycloak, Redmine etc. individual integration microservice exist.
	 * URL for each such microservice will be available at bbcomp table which is a master table.
	 * So URL will be fetched based on the shared component selection done by user and the microservice get invoked by this method.
	 * context path for each such microservice exist in Keycloak Integration or Redmine Integration will be same
	 * Only Domain part will change and that will be available from the bbcomp table.
	 * @param env
	 * @param sharedComp
	 */
	public void handle(Environment env, EnvReqComponent sharedComp, String password) {
		// TODO Auto-generated method stub

		WebClient webClient = createWebClient(sharedComp.getBbComponent().getSgaredCompServUrl());
		// assumption is only one admin user will be created while environment creation
		// is under process
		UserV2 user = userRepo.findByEnvIdAndUserStatusAndUserTypeNot(env.getEnvId(), "active", "user").get(0);
		user.setPassword(password);
		/*
		 * webClient.post().uri("/envrep/{envCode}",env.getEnvCode()).bodyValue(user).
		 * retrieve() .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
		 * clientResponse.bodyToMono(String.class).flatMap(error -> Mono.error(new
		 * RuntimeException("Client error: " + error))))
		 * .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
		 * clientResponse.bodyToMono(String.class).flatMap(error -> Mono.error(new
		 * RuntimeException("Server error: " + error)))) .bodyToMono(String.class)
		 * .map(body -> ResponseEntity.ok(body));
		 */

		webClient.post().uri("/envrep/{envCode}", env.getEnvCode()).bodyValue(user).retrieve().bodyToMono(String.class)
				.doOnSuccess(response -> {
					log.info("Successfully called ..............." + response);
				}).subscribe(s -> logResponse(s), throwable -> logError(throwable));
		
//		log.info("Service call done for the shared componenet............");

	}

	/**
	 * @param sgaredCompServUrl
	 * @return
	 */
	private WebClient createWebClient(String sgaredCompServUrl) {
		return WebClient.builder()
				.baseUrl(sgaredCompServUrl)
				.filter(logReq())
				// .filter(errorHandling())
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.clientConnector(new ReactorClientHttpConnector(configWebClient())).build();

	}

	private HttpClient configWebClient() {
		HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
				.responseTimeout(Duration.ofMillis(20000)).doOnConnected(
						connection -> connection.addHandlerLast(new ReadTimeoutHandler(20000, TimeUnit.MILLISECONDS))
								.addHandlerLast(new WriteTimeoutHandler(20000, TimeUnit.MILLISECONDS)));
		return httpClient;
	}

	private ExchangeFilterFunction logReq() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
//			log.info("Logging request URL {}", clientRequest.url());
			return Mono.just(clientRequest);
		});
	}

	private void logResponse(String s) {
		log.info("response from service handle --> {}", s);
	}

	private void logError(Throwable throwable) {
		log.error("error from service handle --> {}", throwable.getMessage());
	}

}
