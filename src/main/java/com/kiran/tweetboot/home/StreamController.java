package com.kiran.tweetboot.home;

import com.kiran.tweetboot.config.TweetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class StreamController {

	private static String EXPANSIONS = "attachments.media_keys,referenced_tweets.id,author_id";
	private static String MEDIA_FIELDS = "media_key,type,height,preview_image_url,url,width";
	private static String TWEET_FIELDS = "created_at,public_metrics,entities,source,attachments,referenced_tweets,author_id";
	private static String USER_FIELDS = "created_at,description,entities,id,location,name,profile_image_url,public_metrics,url,username,verified";
	private Logger log = LoggerFactory.getLogger(getClass());
	private String baseUrl;

	private TweetClient client;

	public StreamController(@Value("${client.base-url}") String newBaseUrl, TweetClient client) {
		this.baseUrl = newBaseUrl;
		this.client = client;
	}

	@GetMapping("/stream")
	@ResponseBody
	public Flux<String> stream() throws Exception {
		UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(baseUrl).path("/2/tweets/search/stream");
		uri.queryParam("expansions", EXPANSIONS);
		uri.queryParam("media.fields", MEDIA_FIELDS);
		uri.queryParam("tweet.fields", TWEET_FIELDS);
		uri.queryParam("user.fields", USER_FIELDS);
		log.info(uri.build(false).toUri().toString());
		return client
				.build(WebClient.builder(), baseUrl)
				.get()
				.uri(uri.build(false).toUri())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, res -> {
							res.toEntity(String.class).subscribe(
									entity -> log.warn("Client error {}", entity)
							);
							return Mono.error(new HttpClientErrorException(res.statusCode()));
						}
				).bodyToFlux(String.class);
	}
}
