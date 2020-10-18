package com.kiran.tweetboot.unit;

import com.kiran.tweetboot.exception.TwitterAPIError;
import com.kiran.tweetboot.service.StreamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TweetStreamUnitTests {

	@Autowired
	private StreamService streamService;

	@Test
	@DisplayName("Get Stream Rules")
	public void getStreamRuleTest() {
		try {
			UriComponentsBuilder ruleUri = UriComponentsBuilder.newInstance().scheme("https").host("api.twitter.com").path("/2/tweets/search/stream/rules");
			List<String> rules = streamService.getStreamRules(ruleUri.build(false).toUri());
			assertNotNull(rules);
		} catch (TwitterAPIError ex) {
			// this is valid exception
		} catch (Exception ex) {
			fail("Get Stream Rules with exception : " + ex.getMessage());
		}
	}

	@Test
	@DisplayName("Create Stream Rule")
	public void addStreamRuleTest() {
		try {
			ArrayList<String> queries = new ArrayList<String>(Arrays.asList("test", "#test", "tweet", "#tweeter"));
			ArrayList<String> trackTags = new ArrayList<String>(Arrays.asList("test", "#test", "tweet", "#tweeter"));
			UriComponentsBuilder ruleUri = UriComponentsBuilder.newInstance().scheme("https").host("api.twitter.com").path("/2/tweets/search/stream/rules");
			Boolean isAdded = streamService.addStreamRule(ruleUri.build(false).toUri(), queries, trackTags);
			assertTrue(isAdded);
		} catch (TwitterAPIError ex) {
			// this is valid exception
		} catch (Exception ex) {
			fail("Create Stream rule with exception : " + ex.getMessage());
		}
	}

	@Test
	@DisplayName("Delete Stream Rule")
	public void deleteAllStreamRuleTest() {
		try {
			UriComponentsBuilder ruleUri = UriComponentsBuilder.newInstance().scheme("https").host("api.twitter.com").path("/2/tweets/search/stream/rules");
			Boolean idDeleted = streamService.deleteAllStreamRules(ruleUri.build(false).toUri());
			assertTrue(idDeleted);

		} catch (TwitterAPIError ex) {
			// this is valid exception
		} catch (Exception ex) {
			fail("Delete Stream rule with exception : " + ex.getMessage());
		}
	}
}
