package com.kiran.tweetboot.home;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kiran.tweetboot.service.SearchService;
import com.kiran.tweetboot.service.StreamService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

	static private String FIRST_SESSION_ID = "123456789";
	static private String SECOND_SESSION_ID = "987654321";
	@Mock(lenient = true)
	private SearchService searchService;
	@Mock(lenient = true)
	private StreamService streamService;
	@InjectMocks
	private HomeController controller;
	private MockMvc mockMvc;
	@Mock(lenient = true)
	private MockHttpSession session;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		given(session.getId()).willReturn(FIRST_SESSION_ID).willReturn(SECOND_SESSION_ID);
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	@DisplayName("Index Page load test")
	void loadHomePage() throws Exception {

		ArrayList<String> trendings = new ArrayList<>();
		trendings.add("#funny");
		trendings.add("#cool");
		trendings.add("Happy");
		given(searchService.getTrendingHashtags()).willReturn(trendings);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode tweets = mapper.readValue(getClass().getResourceAsStream("/trending_tweets.json"), ObjectNode.class);
		given(searchService.getTweets(any())).willReturn(tweets);

		mockMvc.perform(get("/").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("index"))
				.andExpect(model().attributeExists("tweets"));

		mockMvc.perform(get("/").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("trending_only"))
				.andExpect(model().attributeExists("tweets"));

		mockMvc.perform(get("/").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("trending_only"))
				.andExpect(model().attributeExists("tweets"));
	}

	@Test
	@DisplayName("Fetch next Tweets test")
	void nextPageTrendingTweetsSuccess() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode tweets = mapper.readValue(getClass().getResourceAsStream("/trending_tweets.json"), ObjectNode.class);
		given(searchService.getTweets(any())).willReturn(tweets);

		final String query = "#funny OR #cool OR Happy";
		final String nextToken = "qb232387hegw33908y";

		mockMvc.perform(post("/next")
				.param("query", query)
				.param("next_token", nextToken)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isError", is("N")));
	}

	@Test
	@DisplayName("Subscribe stream test")
	void getAndSubscribeTweetStream() throws Exception {

		ArrayList<String> rules = new ArrayList<>();
		rules.add("1212131231");
		given(streamService.getStreamRules(any())).willReturn(rules);
		given(streamService.deleteAllStreamRules(any(), anyList())).willReturn(true);
		given(streamService.addStreamRule(any(), any(), any())).willReturn(true);

		mockMvc.perform(post("/search")
				.queryParam("filters[]", "#funny", "#cool", "Happy", "no change", "@twitter")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isError", is("N")));
	}

	@Test
	@DisplayName("Subscribe invalid stream test")
	void getAndSubscribeTweetStreamError() throws Exception {

		ArrayList<String> rules = new ArrayList<>();
		rules.add("1212131231");
		given(streamService.getStreamRules(any())).willReturn(rules);
		given(streamService.deleteAllStreamRules(any(), anyList())).willReturn(true);
		given(streamService.addStreamRule(any(), any(), any())).willReturn(true);

		mockMvc.perform(post("/search")
				.queryParam("filters[]", "#funny", "#cool", "Happy", "no change", "@twitter", "#funny", "#cool", "Happy", "no change", "@twitter", "#funny", "#cool", "Happy", "no change", "@twitter", "#funny", "#cool", "Happy", "no change", "@twitter", "#funny", "#cool", "Happy", "no change", "@twitter", "#funny", "#cool", "Happy", "no change", "@twitter")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isError", is("Y")));

		mockMvc.perform(post("/search")
				.queryParam("filters[]", "")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isError", is("Y")));
	}

	@Test
	void stream() {
	}
}