namespace java com.gu.storypackage.model.v1
#@namespace scala com.gu.storypackage.model.v1

/* Describes the types of articles that we can have */

enum ArticleType {
    Article = 1,
    Snap = 2
}

enum Group {
    Included = 1,
    Linked = 2
}

/**
* this stucture represents articles and its overrides
**/
struct Article {

    1: required string id;

    2: required ArticleType articleType;

    3: required Group group

    4: optional string headline;

    5: optional string href;

    6: optional string trailText;

    7: optional string imageSrc;

    8: optional bool isBoosted;

    9: optional bool imageHide;

    10: optional bool showMainVideo;

    11: optional bool showKickerTag;

    12: optional bool showKickerSection;

    13: optional string byline;

    14: optional string customKicker;

    15: optional bool showBoostedHeadline;

    16: optional bool showQuotedHeadline;
}
