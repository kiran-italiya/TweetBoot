package com.kiran.tweetboot;

import com.kiran.tweetboot.config.TweeterRestTemplateInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@SpringBootApplication
public class TweetbootApplication {

	@Value("${tweeter.api.bearer-string}")
	private String bearerString;

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


}
