package com.kiran.tweetboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiran.tweetboot.config.TweeterRestTemplateInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

@SpringBootApplication
public class TweetbootApplication {

	@Value("${tweeter.api.bearer-string}")
	private String bearerString;

	@Value("${tweeter.api.bearer-token}")
	private String bearerToken;

	public static void main(String[] args) {
		SpringApplication.run(TweetbootApplication.class, args);
	}

	@Bean
	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
//		interceptors.add(new TweeterRestTemplateInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE));
		interceptors.add(new TweeterRestTemplateInterceptor("Authorization", bearerString));
		restTemplate.setInterceptors(interceptors);
		return restTemplate;
	}

	@Bean
	public WebClient getWebClient() {

		ObjectMapper decoder = new ObjectMapper();
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
		dateFormat1.setLenient(true);
		decoder.setDateFormat(dateFormat1);

		ObjectMapper encoder = new ObjectMapper();
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		dateFormat2.setTimeZone(TimeZone.getTimeZone("IST"));
		encoder.setDateFormat(dateFormat2);

		return WebClient.builder()
				.codecs(configurer -> {
					configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(encoder, MediaType.APPLICATION_JSON));
					configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(decoder, MediaType.APPLICATION_JSON));
				})
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setBearerAuth(bearerToken);
				})
				.baseUrl("https://api.twitter.com/2/tweets")
				.build();
	}


}
