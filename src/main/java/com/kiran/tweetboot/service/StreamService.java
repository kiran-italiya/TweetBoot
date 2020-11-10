package com.kiran.tweetboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kiran.tweetboot.config.TweetClient;
import com.kiran.tweetboot.exception.TwitterAPIError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class StreamService {

	private TweetClient client;

	private String baseUrl;

	public StreamService(@Value("${client.base-url}") String newBaseUrl, TweetClient client) {
		this.baseUrl = newBaseUrl;
		this.client = client;
	}
	

	public List<String> getStreamRules(URI uri) throws Exception {
		List<String> rulesIdList = new ArrayList<>();

//		TweetClient client = new TweetClient();
		Mono<ObjectNode> response = client
				.build(WebClient.builder(), baseUrl)
				.get()
				.uri(uri)
				.retrieve()
				.bodyToMono(ObjectNode.class);
		ObjectNode result = response.block();
		if (result != null && result.has("data")) {
			Iterator<JsonNode> data = result.withArray("data").elements();
			while (data.hasNext()) {
				rulesIdList.add(data.next().get("id").asText());
			}
		}
		return rulesIdList;
	}

	public boolean addStreamRule(URI uri, List<String> queries, List<String> trackTags) throws Exception {

		ArrayNode addArray = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < queries.size(); i++) {
			addArray.add(JsonNodeFactory.instance.objectNode().put("value", queries.get(i)).put("tag", trackTags.get(i)));
		}
		ObjectNode json = JsonNodeFactory.instance.objectNode();
		json.putArray("add").addAll(addArray);

//		TweetClient client = new TweetClient();
		Mono<ObjectNode> response = client
				.build(WebClient.builder(), baseUrl)
				.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(json), ObjectNode.class)
				.retrieve()
				.bodyToMono(ObjectNode.class);
		ObjectNode result = response.block();
		if (result != null && result.has("meta")) {
			if (result.has("errors")) {
				throw new TwitterAPIError("Invalid query so rule cannot be added");
			}
			return queries.size() == result.get("meta").get("summary").get("created").asInt()
					&& queries.size() == result.get("meta").get("summary").get("valid").asInt();
		}
		return false;
	}

	public boolean deleteAllStreamRules(URI uri, List<String> queries) throws Exception {
		ArrayNode idsArray = JsonNodeFactory.instance.arrayNode();
		if (queries.size() > 0) {
			for (String query : queries) {
				idsArray.add(query);
			}
			ObjectNode json = JsonNodeFactory.instance.objectNode();
			ObjectNode deleteNode = JsonNodeFactory.instance.objectNode();
			deleteNode.putArray("ids").addAll(idsArray);
			json.set("delete", deleteNode);

//			TweetClient client = new TweetClient();
			Mono<ObjectNode> response = client
					.build(WebClient.builder(), baseUrl)
					.post()
					.uri(uri)
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(json)
					.retrieve()
					.bodyToMono(ObjectNode.class);
			ObjectNode result = response.block();
			if (result != null && result.has("meta")) {
				return queries.size() == result.get("meta").get("summary").get("deleted").asInt();
			}
			return false;
		} else {
			return true;
		}
	}

}
