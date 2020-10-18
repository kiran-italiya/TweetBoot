package com.kiran.tweetboot.unit;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kiran.tweetboot.exception.TwitterAPIError;
import com.kiran.tweetboot.service.SearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TweetSearchUnitTests {

	@Autowired
	private SearchService searchService;

	@Test
	@DisplayName("Get Trending hashtags")
	public void trendingHashtagsTest() {
		try {
			ArrayList<String> hashtags = searchService.getTrendingHashtags();
			assertNotNull(hashtags);
			assertTrue(hashtags.size() > 0);
		} catch (TwitterAPIError ex) {
			// this is valid exception
		} catch (Exception ex) {
			fail("Trending hashtag with exception : " + ex.getMessage());
		}
	}


	@Test
	@DisplayName("Get Tweets")
	public void getTweetsTest() {
		try {
			UriComponentsBuilder uri = UriComponentsBuilder.newInstance().scheme("https").host("api.twitter.com").path("/2/tweets/search/recent");
			uri.queryParam("query", "#India OR #USA OR #funny");
			uri.queryParam("max_results", 10);
			ObjectNode tweets = searchService.getTweets(uri.build(false).toUri());
			assertNotNull(tweets);
			assertNotNull(tweets.get("data"));
			assertTrue(tweets.get("data").size() == 10);
		} catch (TwitterAPIError ex) {
			// this is valid exception
		} catch (Exception ex) {
			fail("Get Tweets with exception : " + ex.getMessage());
		}
	}

}
