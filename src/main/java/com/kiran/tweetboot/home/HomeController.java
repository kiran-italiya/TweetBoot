package com.kiran.tweetboot.home;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kiran.tweetboot.exception.InvalidUserDataException;
import com.kiran.tweetboot.exception.TwitterAPIError;
import com.kiran.tweetboot.service.SearchService;
import com.kiran.tweetboot.service.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("/")
public class HomeController {

	private Logger log = LoggerFactory.getLogger(getClass());

	private static Integer MAX_RESULTS = 20;
	private static String EXPANSIONS = "attachments.media_keys,referenced_tweets.id,author_id";
	private static String MEDIA_FIELDS = "media_key,type,height,preview_image_url,url,width";
	private static String TWEET_FIELDS = "created_at,public_metrics,entities,source,attachments,referenced_tweets,author_id";
	private static String USER_FIELDS = "created_at,description,entities,id,location,name,profile_image_url,public_metrics,url,username,verified";

	private static String TWITTER_HANDLE_REGEX = "(?<=^|(?<=[^a-zA-Z0-9-_\\.]))@([A-Za-z]+[A-Za-z0-9-_]+)";
	private static String TWITTER_HASHTAG_REGEX = "(?<=^|(?<=[^a-zA-Z0-9-_\\.]))#([A-Za-z]+[A-Za-z0-9-_]+)";

	private static String currSessionId = null;
	private static Date lastDate = new Date();

	private Integer TIMEOUT = 600000;


	@Autowired
	private SearchService searchService;

	@Autowired
	private StreamService streamService;

	@Autowired
	private HttpSession session;

	@GetMapping()
	public String loadHomePage(Model model) {
		try {

			if (currSessionId != null && new Date().getTime() - lastDate.getTime() > TIMEOUT) {
				currSessionId = null;
			}

			ObjectNode tweets = null;
			ArrayList<String> hashtags = searchService.getTrendingHashtags();
			UriComponentsBuilder uri = UriComponentsBuilder.newInstance().scheme("https").host("api.twitter.com").path("/2/tweets/search/recent");
			if (hashtags.size() > 0) {
				StringJoiner tags = new StringJoiner(" OR ");
				for (String hashtag : hashtags.subList(0, hashtags.size() > 5 ? 5 : hashtags.size())) {
					tags.add(hashtag);
				}
				System.out.println(tags.toString());
				uri.queryParam("query", tags.toString());
				uri.queryParam("expansions", EXPANSIONS);
				uri.queryParam("media.fields", MEDIA_FIELDS);
				uri.queryParam("tweet.fields", TWEET_FIELDS);
				uri.queryParam("user.fields", USER_FIELDS);
				uri.queryParam("max_results", MAX_RESULTS);
				System.out.println(uri.build(false).toUri());
				tweets = searchService.getTweets(uri.build(false).toUri());
				tweets.put("query", tags.toString());
			}
			model.addAttribute("tweets", tweets);
			if (currSessionId == null) {
				currSessionId = session.getId();
				lastDate = new Date();
				return "index";
			} else {
				if (session.getId().equals(currSessionId)) {
					return "index";
				} else {
					return "trending_only";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "404";
	}

	@PostMapping("/next")
	@ResponseBody
	public ResponseEntity<Object> nextPageTrendingTweets(@RequestParam(value = "query", required = true) String query, @RequestParam(value = "next_token", required = true) String nextToken) {
		HashMap<String, Object> responseMap = new HashMap<>();
		ObjectNode tweets = JsonNodeFactory.instance.objectNode();
		try {
			UriComponentsBuilder uri = UriComponentsBuilder.newInstance().scheme("https").host("api.twitter.com").path("/2/tweets/search/recent");
			uri.queryParam("query", query);
			uri.queryParam("expansions", EXPANSIONS);
			uri.queryParam("media.fields", MEDIA_FIELDS);
			uri.queryParam("tweet.fields", TWEET_FIELDS);
			uri.queryParam("user.fields", USER_FIELDS);
			uri.queryParam("next_token", nextToken);
			uri.queryParam("max_results", MAX_RESULTS);
			tweets = searchService.getTweets(uri.build(false).toUri());
			tweets.put("query", query);
			responseMap.put("isError", "N");
			responseMap.put("tweetsData", tweets);
			return ResponseEntity.ok(responseMap);
		} catch (TwitterAPIError e) {
			responseMap.put("error", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		responseMap.put("isError", "Y");
		return ResponseEntity.badRequest().body(responseMap);
	}

	@PostMapping("/search")
	@ResponseBody
	public ResponseEntity<Object> getAndSubscribeTweetStream(@RequestParam("filters[]") List<String> filterWords) {
		HashMap<String, String> responseMap = new HashMap<>();
		try {
			boolean ruleAdded = false;
			List<String> queries = new ArrayList<>();
			List<String> trackTags = new ArrayList<>();
			if (filterWords.size() > 25) {
				responseMap.put("errorString", "Twitter handles, keywords and hashtags combined should not be greater than 25");
				throw new InvalidUserDataException("Invalid Input From User");
			} else if (filterWords.size() == 0) {
				responseMap.put("errorString", "At least one filter keyword required");
				throw new InvalidUserDataException("Invalid Input From User");
			}
			for (String word : filterWords) {
				if (word.startsWith("@") && !word.matches(TWITTER_HANDLE_REGEX)) {
					responseMap.put("errorString", word + " : Invalid Characters in Tweeter Handle");
					throw new InvalidUserDataException("Invalid Input From User");
				}
				if (word.startsWith("#") && !word.matches(TWITTER_HASHTAG_REGEX)) {
					responseMap.put("errorString", word + " : Invalid Characters in Hashtag");
					throw new InvalidUserDataException("Invalid Input From User");
				}
				if (word.contains(" ")) {
					queries.add("\"" + word.trim() + "\"");
					trackTags.add(word.trim());
				} else if (word.startsWith("@")) {
					String newWord = word.substring(1);
					queries.add("from:" + newWord + " OR to:" + newWord);
					trackTags.add(word);
				} else {
					queries.add(word.trim());
					trackTags.add(word);
				}
			}

			UriComponentsBuilder ruleUri = UriComponentsBuilder.newInstance().scheme("https").host("api.twitter.com").path("/2/tweets/search/stream/rules");
			List<String> rules = streamService.getStreamRules(ruleUri.build(false).toUri());
			if (rules.size() == 0) {
				responseMap.put("isError", "N");
				return ResponseEntity.ok(responseMap);
			}
			if (streamService.deleteAllStreamRules(ruleUri.build(false).toUri(), rules)) {
				ruleAdded = streamService.addStreamRule(ruleUri.build(false).toUri(), queries, trackTags);
			} else {
				throw new TwitterAPIError("Error deleting old stream rules");
			}
			if (ruleAdded) {
				responseMap.put("isError", "N");
				return ResponseEntity.ok(responseMap);
			} else {
				responseMap.put("errorString", "Error Occurred in Adding Rules");
			}
		} catch (Exception e) {
			log.error("{}", e.getMessage());
		}
		responseMap.put("isError", "Y");
		return ResponseEntity.badRequest().body(responseMap);
	}
}
