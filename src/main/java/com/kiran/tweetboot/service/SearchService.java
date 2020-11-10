package com.kiran.tweetboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kiran.tweetboot.exception.TwitterAPIError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;

@Service
public class SearchService {

	@Autowired
	private RestTemplate restTemplate;

	public ArrayList<String> getTrendingHashtags() throws Exception {
		Integer woeId = 23424848;
		StringBuilder query = new StringBuilder("https://api.twitter.com/1.1/trends/place.json?id=");
		query.append(woeId);
		ArrayNode json = restTemplate.getForObject(query.toString(), ArrayNode.class);
		ArrayList<String> hashtags = new ArrayList<>();
		if (json != null && !json.isEmpty()) {
			JsonNode trends = json.get(0).get("trends");
			if (trends.isArray()) {
				for (JsonNode trend : trends) {
					String tag = trend.get("name").asText();
					if (tag.matches("^[A-Za-z0-9 _#-]+$")) {
						hashtags.add(tag);
					}
				}
			}
		} else {
			throw new TwitterAPIError("Error occurred while fetching trending hashtags");
		}
		return hashtags;
	}

	public ObjectNode getTweets(URI url) throws TwitterAPIError {
		ObjectNode tweetsJson = null;
		try {
			tweetsJson = restTemplate.getForObject(url, ObjectNode.class);
			if (tweetsJson != null && !tweetsJson.isEmpty()) {
				return tweetsJson;
			} else {
				throw new TwitterAPIError("Error occurred while fetching Tweets");
			}
		} catch (Exception e) {
			if (tweetsJson != null && tweetsJson.has("error")) {
				throw new TwitterAPIError(tweetsJson.get("error").asText());
			}
		}
		throw new TwitterAPIError("Error occurred while fetching Tweets");
	}

}
