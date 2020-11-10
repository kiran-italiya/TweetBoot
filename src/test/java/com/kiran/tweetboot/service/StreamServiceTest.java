package com.kiran.tweetboot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kiran.tweetboot.config.TweetClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StreamServiceTest {

	private StreamService streamService;

	private MockWebServer mockWebServer;

	private UriComponentsBuilder ruleUri;

	@BeforeEach
	public void setup() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		streamService = new StreamService("http://localhost:" + mockWebServer.getPort(), new TweetClient());
		ruleUri = UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(mockWebServer.getPort()).path("/tweets/search/stream/rules");

	}

	@AfterEach
	public void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	@DisplayName("Fetch stream rules test")
	public void getStreamRulesTest() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rulesJson = mapper.readValue(new FileInputStream("src/test/resources/stream_rules.json"), ObjectNode.class);

		MockResponse response = new MockResponse()
				.addHeader("Content-Type", "application/json")
				.setBody(mapper.writeValueAsString(rulesJson));
		mockWebServer.enqueue(response);

		List<String> rules = streamService.getStreamRules(ruleUri.build(false).toUri());
		assertThat(rules).isNotNull();
	}

	@Test
	@DisplayName("Add stream rules test")
	public void addStreamRuleTest() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rulesJson = mapper.readValue(new FileInputStream("src/test/resources/added_stream_rule.json"), ObjectNode.class);

		MockResponse response = new MockResponse()
				.addHeader("Content-Type", "application/json")
				.setBody(mapper.writeValueAsString(rulesJson));
		mockWebServer.enqueue(response);

		ArrayList<String> queries = new ArrayList<>();
		queries.add("#kiran");
		ArrayList<String> trackTags = new ArrayList<>();
		trackTags.add("tweets with #kiran");
		Boolean isAdded = streamService.addStreamRule(ruleUri.build(false).toUri(), queries, trackTags);

		RecordedRequest request = mockWebServer.takeRequest();
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getBodySize()).isGreaterThan(0);
		assertThat(isAdded).isTrue();
	}

	@Test
	@DisplayName("Delete stream rules test")
	public void deleteAllStreamRulesTest() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rulesJson = mapper.readValue(new FileInputStream("src/test/resources/delete_rule_response.json"), ObjectNode.class);

		MockResponse response = new MockResponse()
				.addHeader("Content-Type", "application/json")
				.setBody(mapper.writeValueAsString(rulesJson));
		mockWebServer.enqueue(response);

		ArrayList<String> rules = new ArrayList<>();
		rules.add("43423523523");
		Boolean isAdded = streamService.deleteAllStreamRules(ruleUri.build(false).toUri(), rules);

		RecordedRequest request = mockWebServer.takeRequest();
		assertThat(request.getMethod()).isEqualTo("POST");
		assertThat(request.getBodySize()).isGreaterThan(0);
		assertThat(isAdded).isTrue();
	}
}