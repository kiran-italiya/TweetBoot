package com.kiran.tweetboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Service
public class TweetClient {

	private WebClient webClient;

	@Value("${tweeter.api.bearer-token}")
	private String bearerToken;

	public WebClient build(WebClient.Builder builder, @Value("${client.base-url}") String baseUrl) {
		ObjectMapper decoder = new ObjectMapper();
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
		dateFormat1.setLenient(true);
		decoder.setDateFormat(dateFormat1);

		ObjectMapper encoder = new ObjectMapper();
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		dateFormat2.setTimeZone(TimeZone.getTimeZone("IST"));
		encoder.setDateFormat(dateFormat2);

		this.webClient = builder
				.codecs(configurer -> {
					configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(encoder, MediaType.APPLICATION_JSON));
					configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(decoder, MediaType.APPLICATION_JSON));
				})
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setBearerAuth(bearerToken);
				})
				.baseUrl(baseUrl)
				.build();

		return webClient;
	}
}
