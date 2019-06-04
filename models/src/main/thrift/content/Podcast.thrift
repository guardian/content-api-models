namespace scala com.gu.contentapi.client.model.v1


struct PodcastCategory {

    1: required string main

    2: optional string sub
}

struct Podcast {

    1: required string linkUrl

    2: required string copyright

    3: required string author

    4: optional string subscriptionUrl

    5: required bool explicit

    6: optional string image

    7: optional list<PodcastCategory> categories

    8: optional string podcastType

    9: optional string googlePodcastsUrl

    10: optional string spotifyUrl
}
