package com.kiran.tweetboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kiran.tweetboot.exception.InvalidUserDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TweetSearchIntegrationTestsIT {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@BeforeEach
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	@DisplayName("UI page data test")
	public void homePageTests() throws Exception {

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andReturn();
		assertEquals(result.getResponse().getStatus(), 200, "Trending data fetch failed");

		ObjectNode tweets = ((ObjectNode) result.getRequest().getAttribute("tweets"));
		String query = tweets.get("query").asText();
		assertTrue(tweets.size() > 0);
		assumeTrue(tweets.get("meta").has("next_token"));
		String nextToken = tweets.get("meta").get("next_token").asText();

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("query", query);
		map.add("next_token", nextToken);

		result = mockMvc.perform(MockMvcRequestBuilders.post("/next")
				.contentType(MediaType.APPLICATION_JSON)
				.params(map)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isError", is("N")))
				.andReturn();
		assertEquals(result.getResponse().getStatus(), 200, "Next trending fetch failed");
	}

	@Test
	@DisplayName("Stream rule reset and add")
	public void streamRuleResetAndAdd() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<String> filterWords = new ArrayList<String>(Arrays.asList("#India", "#USA", "tweet", "twitter", "funny"));
			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			filterWords.forEach(word -> map.add("filters[]", word));
			MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/search")
					.contentType(MediaType.APPLICATION_JSON)
					.params(map)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isError", is("N")))
					.andReturn();
			assertEquals(result.getResponse().getStatus(), 200, "Rules reset and add failed");
		} catch (InvalidUserDataException ex) {
			// this is valid exception
		} catch (Exception ex) {
			fail("Stream rule reset and add failed with exception : ", ex);
		}
	}
}
