/**
 * ArgoClientErrorDecoder.java
 *
 * @author AC
 * @date 06-May-2025
 */
package com.nnp.envrep.exception;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Response;
import feign.codec.ErrorDecoder;
import feign.codec.ErrorDecoder.Default;

/**
 * ArgoClientErrorDecoder.java
 *
 * @author AC
 * @date 06-May-2025
 */
public class ArgoClientErrorDecoder implements ErrorDecoder {

	private final ErrorDecoder errorDecoder = new Default();

	@Override
	public Exception decode(String methodKey, Response response) {
		ArgoApiExceptionMessage message;
		try (InputStream bodyIs = response.body().asInputStream()) {
			ObjectMapper mapper = new ObjectMapper();
			message = mapper.readValue(bodyIs, ArgoApiExceptionMessage.class);
		} catch (IOException e) {
			return new Exception(e.getMessage());
		}
		return switch (response.status()) {
		case 400 -> new ArgoException(message.getMessage() != null ? message.getCode()+" - " + message.getMessage() : "Bad Request");
		case 404 -> new ArgoException(message.getMessage() != null ? message.getCode()+" - " + message.getMessage() : "Not found");
		case 409 ->
			new ArgoException(message.getMessage() != null ? message.getCode()+" - " + message.getMessage() : "Exception Occured");
		default -> errorDecoder.decode(methodKey, response);
		};
		
	}

}
