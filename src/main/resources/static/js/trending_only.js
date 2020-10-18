let query = null;
let next_token = null;

let event_source = null;

// tweetsData = JSON.parse(tweetsData);
query = tweetsData.query;
next_token = tweetsData.meta.next_token;
marshalTweetsAndAppend($("#tweets-feed"));

$(document).ready(function () {

    $(window).scroll(function () {
        if ($(window).scrollTop() + $(window).height() >= $(document).height()) {
            $(".loading").show();
            if (next_token) {
                ajaxFunc("POST", "/next", {
                    query: query,
                    next_token: next_token
                }, 2, function (data, statusText, jqXHR) {
                    let resp = jqXHR.responseText || data.responseText;
                    if (resp) {
                        resp = JSON.parse(resp);
                        // if (resp.isError === 'N') {
                        // tweetsData = JSON.parse(resp.tweetsData);
                        tweetsData = resp;
                        marshalTweetsAndAppend($("#tweets-feed"));
                        $(".loading").hide();
                        // } else {
                        //     errorMessage(resp.errorType, resp.errorString, resp.errorTitle);
                        // }
                    } else {
                        errorMessage("error", "Something Went Wrong on Server-Side", "Ooops!");
                    }
                })
            }
        }
    });

});

function marshalTweetsAndAppend(tweetsFeedSelector) {
    if (tweetsData) {
        query = tweetsData.query;
        next_token = tweetsData.meta ? tweetsData.meta.next_token : null;

        let users_dict = makeUsersDict(tweetsData.includes.users);
        let media_dict = makeMediaDict(tweetsData.includes.media);

        for (let tweet of tweetsData.data) {

            let author_id = tweet.author_id;

            tweetsFeedSelector.append(`
            <ul class="bg-white rounded-lg shadow mb-8">
                    <div>
                        <li class="px-6 py-5 border-b border-gray-200">
                            <div class="flex w-full">
                                <div class="flex-shrink-0 mr-5">
                                    <div class="cursor-pointer font-bold w-12 h-12 bg-gray-300 flex items-center justify-center rounded-full">
                                        <img class="flex items-center justify-center rounded-full"
                                             src="${users_dict[tweet.author_id].profile_image_url}">
                                    </div>
                                </div>
                                <div class="flex-1">
                                    <div>
                                        <strong class="font-bold text-gray-800 mr-2">${users_dict[tweet.author_id].name}</strong>
                                        <span class="text-gray-600">@${users_dict[tweet.author_id].username}</span>
                                        <span class="mx-1 text-gray-500">&bull;</span>
                                        <span class="text-gray-600">${formatDate(tweet.created_at)}</span>
                                    </div>

                                    <div class="mb-4">
                                        <p class="text-gray-700">${tweet.text.replaceAll("\n", "</br>")}</p>
                                    </div>

                                    <div class="relative w-auto mb-2 border rounded-lg relative bg-gray-100 mb-4 shadow-inset overflow-hidden">
                                        ${allMediaHtmlElements(tweet, media_dict)}
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

function allMediaHtmlElements(tweet, media_dict) {
    let str = "";
    if (tweet.attachments && tweet.attachments.media_keys) {
        for (let media_id of tweet.attachments.media_keys) {
            str += `<div class="gg-container">
                        <div class="gg-box square-gallery" style="margin: 0">
                            <img class="object-cover w-full" src="${media_dict[media_id].url}"/>
                        </div>
                    </div>`;
        }
    }
    return str;
}