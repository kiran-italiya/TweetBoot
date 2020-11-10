package com.kiran.tweetboot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kiran.tweetboot.exception.TwitterAPIError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileInputStream;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

	@Mock(lenient = true)
	private RestTemplate restTemplate;

	@InjectMocks
	private SearchService searchService;

	@Test
	@DisplayName("Fetch trending hashtags test")
	void getTrendingHashtagsTest() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode hashtags = mapper.readValue(new FileInputStream("src/test/resources/trends_data.json"), ArrayNode.class);
		given(restTemplate.getForObject("https://api.twitter.com/1.1/trends/place.json?id=23424848", ArrayNode.class)).willReturn(hashtags);
		assertDoesNotThrow(() -> {
			ArrayList<String> result = searchService.getTrendingHashtags();
			assertThat(result).isNotEmpty();
		});
	}

	@Test
	@DisplayName("Fetch tweets test")
	void getTweetsTest() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode tweets = mapper.readValue(new FileInputStream("src/test/resources/trending_tweets.json"), ObjectNode.class);
		given(restTemplate.getForObject(any(), eq(ObjectNode.class))).willReturn(tweets);
		assertDoesNotThrow(() -> {
			UriComponentsBuilder uri = UriComponentsBuilder.newInstance().scheme("https").host("api.twitter.com").path("/2/tweets/search/recent");
			ObjectNode result = searchService.getTweets(uri.build(false).toUri());
			assertThat(result).isNotEmpty();
		});
	}

	@Test
	@DisplayName("Fetch invalid tweets test")
	void getTweetsTestError() {
		given(restTemplate.getForObject(any(), any())).willReturn(new ObjectNode(JsonNodeFactory.instance));
		assertThrows(TwitterAPIError.class, () -> {
			UriComponentsBuilder uri = UriComponentsBuilder.newInstance().scheme("https").host("api.twitter.com").path("/2/tweets/search/recent");
			searchService.getTweets(uri.build(false).toUri());
		});
	}
}