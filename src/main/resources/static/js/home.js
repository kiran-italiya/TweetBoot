let query = null;
let nextToken = null;

let eventSource = null;

const TWITTER_HANDLE_REGEX = /(?<=^|(?<=[^a-zA-Z0-9-_.]))@([A-Za-z]+[A-Za-z0-9-_]+)/;
const TWITTER_HASHTAG_REGEX = /(?<=^|(?<=[^a-zA-Z0-9-_.]))#([A-Za-z]+[A-Za-z0-9-_]+)/;

query = tweetsData.query;
nextToken = tweetsData.meta.next_token;
$("#stream-status").text("Not Connected");
$("#stream_disconnect_div").hide();
marshalTweetsAndAppend($("#tweets-feed"), tweetsData);

$(document).ready(function () {

    $("#twitter-handles,#keywords-hashtags").tagsinput({
        tagClass: function (item) {
            return (item.length > 10 ? 'big' : 'small');
        },
        trimValue: true
    });

    $(window).scroll(function () {
        if ($(window).scrollTop() + $(window).height() >= $(document).height()) {
            if(query!=null){
                $(".loading").show();
                if (nextToken) {
                    ajaxFunc("POST", "/next", {
                        query: query,
                        next_token: nextToken
                    }, 2, function (data, statusText, jqXHR) {
                        let resp = jqXHR.responseText || data.responseText;
                        if (resp) {
                            resp = JSON.parse(resp);
                            if (resp.isError === 'N') {
                                marshalTweetsAndAppend($("#tweets-feed"), resp.tweetsData);
                                $(".loading").hide();
                            } else {
                                errorMessage("error", "Unable to Load Next Tweets", "Ooops!");
                            }
                        } else {
                            errorMessage("error", "Something Went Wrong on Server-Side", "Ooops!");
                        }
                    })
                }
            }
        }
    });

    $(window).bind('beforeunload', function () {
        disconnect();
    });

    initializeImagePreview();

    $("#search-btn").click(function () {
        if (!isArrayAndNotEmpty($("#twitter-handles").tagsinput('items')) && !isArrayAndNotEmpty($("#keywords-hashtags").tagsinput('items'))) {
            errorMessage("error", "Please Enter Valid Input", "Error");
            return false;
        } else if ( $("#twitter-handles").tagsinput('items').length + $("#keywords-hashtags").tagsinput('items').length > 25) {
            errorMessage("error", "Twitter handles, keywords and hashtags combined should not be greater than 25", "Ooops!");
            return false;
        } else if ( !areTweeterHandlesValid()) {
            // already displayed message
            return false;
        } else if ( !areKeywordsValid()) {
            // already displayed message
            return false;
        } else {
            $("#tweets-feed").empty();
            $(".loading").show();
            ajaxFunc("POST", "/search", {
                filters: mergeFilterData($("#twitter-handles").tagsinput('items'), $("#keywords-hashtags").tagsinput('items'))
            }, 2, function (data, statusText, jqXHR) {
                let resp = jqXHR.responseText || data.responseText;
                if (resp) {
                    resp = JSON.parse(resp);
                    if (resp.isError === 'N') {
                        errorMessage("success", "Filter Rules Updated", "Yaay!");
                        connect();
                    } else {
                        disconnect();
                        errorMessage("error", resp.errorString, "Ooops!");
                    }
                } else {
                    errorMessage("error", "Something Went Wrong on Server-Side", "Ooops!");
                }
            })
        }
    });

    $("#stream_connect_btn").click(function () {
        connect();
    });

    $("#stream_disconnect_btn").click(function () {
        disconnect();
    });

});

function marshalTweetsAndAppend(tweetsFeedSelector, tweetsData) {
    if (tweetsData) {
        query = tweetsData.query;
        nextToken = tweetsData.meta ? tweetsData.meta.next_token : null;

        let usersDict = makeUsersDict(tweetsData.includes.users);
        let mediaDict = makeMediaDict(tweetsData.includes.media);

        for (let tweet of tweetsData.data) {

            tweetsFeedSelector.append(`
            <ul class="bg-white rounded-lg shadow mb-8">
                    <div>
                        <li class="px-6 py-5 border-b border-gray-200">
                            <div class="flex w-full">
                                <div class="flex-shrink-0 mr-5">
                                    <div class="cursor-pointer font-bold w-12 h-12 bg-gray-300 flex items-center justify-center rounded-full">
                                        <img class="flex items-center justify-center rounded-full"
                                             src="${usersDict[tweet.author_id].profile_image_url}">
                                    </div>
                                </div>
                                <div class="flex-1">
                                    <div>
                                        <strong class="font-bold text-gray-800 mr-2">${usersDict[tweet.author_id].name}</strong>
                                        <span class="text-gray-600">@${usersDict[tweet.author_id].username}</span>
                                        <span class="mx-1 text-gray-500">&bull;</span>
                                        <span class="text-gray-600">${formatDate(tweet.created_at)}</span>
                                    </div>

                                    <div class="mb-4">
                                        <p class="text-gray-700">${tweet.text.replaceAll("\n", "</br>")}</p>
                                    </div>

                                    <div class="relative w-auto mb-2 border rounded-lg relative bg-gray-100 mb-4 shadow-inset overflow-hidden">
                                        ${allMediaHtmlElements(tweet, mediaDict)}
                                    </div>

                                    <div class="flex items-center w-full">
                                        <div class="w-1/3 flex items-center">
                                            <div class="cursor-pointer hover:bg-gray-200 inline-flex p-2 rounded-full duration-200 transition-all ease-in-out">
                                                <svg class="h-6 w-6 text-gray-500" fill="none" viewBox="0 0 24 24"
                                                     stroke="currentColor">
                                                    <path stroke-linecap="round" stroke-linejoin="round"
                                                          stroke-width="2"
                                                          d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
                                                </svg>
                                            </div>
                                            <div class="ml-1 leading-none inline-flex text-gray-600">
                                                ${tweet.public_metrics.quote_count}
                                            </div>
                                        </div>
                                        <div class="w-1/3 flex items-center">
                                            <div class="cursor-pointer hover:bg-gray-200 inline-flex p-2 rounded-full duration-200 transition-all ease-in-out">
                                                <svg class="h-6 w-6 text-gray-500" fill="none" viewBox="0 0 24 24"
                                                     stroke="currentColor">
                                                    <path stroke-linecap="round" stroke-linejoin="round"
                                                          stroke-width="2"
                                                          d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/>
                                                </svg>
                                            </div>
                                            <div class="ml-1 leading-none inline-flex text-gray-600">
                                                ${tweet.public_metrics.retweet_count}
                                            </div>
                                        </div>

                                        <div class="w-1/3 flex items-center">
                                            <div class="cursor-pointer hover:bg-gray-200 inline-flex p-2 rounded-full duration-200 transition-all ease-in-out">
                                                <svg class="h-6 w-6 text-gray-500" fill="none" viewBox="0 0 24 24"
                                                     stroke="currentColor">
                                                    <path stroke-linecap="round" stroke-linejoin="round"
                                                          stroke-width="2"
                                                          d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                                                </svg>
                                            </div>
                                            <div class="ml-1 text-gray-600 leading-none inline-flex">
                                                ${tweet.public_metrics.like_count}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </li>
                    </div>
                </ul>
        `);
        }
    }
}

function marshalTweetAndPrepend(tweetsFeedSelector, tweetsData) {
    if (tweetsData) {
        let usersDict = makeUsersDict(tweetsData.includes.users);
        let mediaDict = makeMediaDict(tweetsData.includes.media);

        let tweet = tweetsData.data;

        let text = tweet.text.replaceAll("\n", "</br>");
        let matched = [];
        for(let obj of tweetsData.matching_rules){
            matched.push(obj.tag);
        }
        text = text.replaceAll(new RegExp('(' + matched.join('|') + ')', "ig"), '<b>$&</b>');

        tweetsFeedSelector.prepend(`
            <ul class="bg-white rounded-lg shadow mb-8" tweet-id="${tweet.id}">
                    <div>
                        <li class="px-6 py-5 border-b border-gray-200">
                            <div class="flex w-full">
                                <div class="flex-shrink-0 mr-5">
                                    <div class="cursor-pointer font-bold w-12 h-12 bg-gray-300 flex items-center justify-center rounded-full">
                                        <img class="flex items-center justify-center rounded-full"
                                             src="${usersDict[tweet.author_id].profile_image_url}">
                                    </div>
                                </div>
                                <div class="flex-1">
                                    <div>
                                        <strong class="font-bold text-gray-800 mr-2">${usersDict[tweet.author_id].name}</strong>
                                        <span class="text-gray-600">@${usersDict[tweet.author_id].username}</span>
                                        <span class="mx-1 text-gray-500">&bull;</span>
                                        <span class="text-gray-600">${formatDate(tweet.created_at)}</span>
                                    </div>
                                    
                                    <div class="mb-4">
                                        <p class="text-gray-700"><i>Matched</i> : <b>${matched.toString()}</b></p>
                                    </div>

                                    <div class="mb-4">
                                        <p class="text-gray-700">${text}</p>
                                    </div>

                                    <div class="relative w-auto mb-2 border rounded-lg relative bg-gray-100 mb-4 shadow-inset overflow-hidden">
                                        ${allMediaHtmlElements(tweet, mediaDict)}
                                    </div>

                                    <div class="flex items-center w-full">
                                        <div class="w-1/3 flex items-center">
                                            <div class="cursor-pointer hover:bg-gray-200 inline-flex p-2 rounded-full duration-200 transition-all ease-in-out">
                                                <svg class="h-6 w-6 text-gray-500" fill="none" viewBox="0 0 24 24"
                                                     stroke="currentColor">
                                                    <path stroke-linecap="round" stroke-linejoin="round"
                                                          stroke-width="2"
                                                          d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
                                                </svg>
                                            </div>
                                            <div class="ml-1 leading-none inline-flex text-gray-600">
                                                ${tweet.public_metrics.quote_count}
                                            </div>
                                        </div>
                                        <div class="w-1/3 flex items-center">
                                            <div class="cursor-pointer hover:bg-gray-200 inline-flex p-2 rounded-full duration-200 transition-all ease-in-out">
                                                <svg class="h-6 w-6 text-gray-500" fill="none" viewBox="0 0 24 24"
                                                     stroke="currentColor">
                                                    <path stroke-linecap="round" stroke-linejoin="round"
                                                          stroke-width="2"
                                                          d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/>
                                                </svg>
                                            </div>
                                            <div class="ml-1 leading-none inline-flex text-gray-600">
                                                ${tweet.public_metrics.retweet_count}
                                            </div>
                                        </div>

                                        <div class="w-1/3 flex items-center">
                                            <div class="cursor-pointer hover:bg-gray-200 inline-flex p-2 rounded-full duration-200 transition-all ease-in-out">
                                                <svg class="h-6 w-6 text-gray-500" fill="none" viewBox="0 0 24 24"
                                                     stroke="currentColor">
                                                    <path stroke-linecap="round" stroke-linejoin="round"
                                                          stroke-width="2"
                                                          d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                                                </svg>
                                            </div>
                                            <div class="ml-1 text-gray-600 leading-none inline-flex">
                                                ${tweet.public_metrics.like_count}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </li>
                    </div>
                </ul>
        `);
        $("#tweets-feed > ul:gt(19)").remove();
    }
}

function makeUsersDict(users) {
    let usersDict = {};
    if (users) {
        for (let user of users) {
            usersDict[user.id] = user;
        }
    }
    return usersDict;
}

function makeMediaDict(media) {
    let mediaDict = {};
    if (media) {
        for (let med of media) {
            mediaDict[med.media_key] = med;
        }
    }
    return mediaDict;
}

function allMediaHtmlElements(tweet, mediaDict) {
    let str = "";
    if (tweet.attachments && tweet.attachments.media_keys) {
        if (tweet.attachments.media_keys.length > 0) {
            str += `<div class="gg-container">`;
            for (let mediaId of tweet.attachments.media_keys) {
                if (mediaDict[mediaId].type === "photo") {
                    str += `<div class="gg-box square-gallery" style="margin: 0">
                                <a href="#" class="zoom img-prev" data-toggle="modal" data-target="#lightbox"> 
                                <img class="object-cover w-full" src="${mediaDict[mediaId].url}" alt="..."/>
                                <span class="overlay"><i class="glyphicon glyphicon-fullscreen"></i></span></a>
                            </div>`;
                } else if (mediaDict[mediaId].type === "animated_gif" || mediaDict[mediaId].type === "video"){
                    str += `<div class="gg-box square-gallery" style="margin: 0">
                                <a href="#" class="zoom img-prev" data-toggle="modal" data-target="#lightbox"> 
                                <img class="object-cover w-full" src="${mediaDict[mediaId].preview_image_url}" alt="..."/>
                                <span class="overlay"><i class="glyphicon glyphicon-fullscreen"></i></span></a>
                            </div>`;
                }
            }
            str += `</div>`;
        }

    }
    return str;
}

function isArrayAndNotEmpty(array) {
    return (Array.isArray(array) && array.length > 0);
}

function mergeFilterData(handles, keywords) {
    let mergedArray = [];
    if(handles){
        for (let handle of handles) {
            mergedArray.push("@" + handle);
        }
    }
    if(keywords){
        for (let word of keywords) {
            mergedArray.push(word);
        }
    }
    return mergedArray;
}

function connect() {
    if(eventSource==null || eventSource.readyState !== eventSource.OPEN){
        $("#stream-status").text("Connecting...");
        $("#stream_connect_div").hide();
        let loading = $(".loading");
        if (eventSource != null) {
            eventSource.close();
        }
        eventSource = new EventSource("/stream");
        eventSource.onopen = function () {
            console.log("SUCCESS: connected");
            setStatus(true);
        };
        eventSource.addEventListener("message", function (event) {
            loading.hide();
            if(event.data && event.data.trim().length>0){
                marshalTweetAndPrepend($("#tweets-feed"), JSON.parse(event.data));
            }
        });
        eventSource.onerror = function () {
            eventSource.close();
            errorMessage("error", "Error in Streaming Live Tweets", "Oops!");
            setStatus(false);
        }
    }
}

function disconnect() {
    if (eventSource != null) {
        eventSource.close();
    }
    setStatus(false);
}

function setStatus(status) {
    if (status === true) {
        query=null;
        $("#stream-status").removeClass("text-danger").addClass("text-success").text("Connected");
        $("#stream_connect_div").hide();
        $("#stream_disconnect_div").show();
        $("#tweets-feed").empty();
        $(".loading").show();
    } else {
        $("#stream-status").removeClass("text-success").addClass("text-danger").text("Not Connected");
        $("#stream_disconnect_div").hide();
        $("#stream_connect_div").show();
    }
}

function areTweeterHandlesValid(handles) {
    let isValid = true;
    if(handles){
        for(let handle of handles){
            if(!TWITTER_HANDLE_REGEX.test(handle)){
                isValid = false;
                errorMessage("error", handle + " : Invalid Twitter Handle", "Oops!");
            }
        }
    }
    return isValid;
}

function areKeywordsValid(keywords) {
    let isValid = true;
    if(keywords){
        for(let keyword of keywords){
            if(keyword.slice(0,1)==="#"){
                if(!TWITTER_HASHTAG_REGEX.test(keyword)){
                    isValid = false;
                    errorMessage("error", keyword + " : Invalid Hashtag", "Oops!");
                }
            }
        }
    }
    return isValid;
}