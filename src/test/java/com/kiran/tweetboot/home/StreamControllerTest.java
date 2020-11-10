package com.kiran.tweetboot.home;

import com.kiran.tweetboot.config.TweetClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.AsyncListener;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class StreamControllerTest {

	private MockMvc mockMvc;

	private StreamController controller;


	@BeforeEach
	public void setUp() {
		controller = new StreamController("https://api.twitter.com", new TweetClient());
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}


	@Test
	@DisplayName("Stream tweets test")
	public void tweetsStreamingTest() throws Exception {

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/stream"))
				.andExpect(request().asyncStarted())
				.andReturn();

		MockAsyncContext ctx = (MockAsyncContext) result.getRequest().getAsyncContext();
//			ctx.setTimeout(1500);
		for (AsyncListener listener : ctx.getListeners()) {
			listener.onTimeout(null);
		}
		mockMvc.perform(asyncDispatch(result))
				.andExpect(status().isOk());
	}

}
