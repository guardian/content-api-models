namespace java com.gu.story.model.v1
#@namespace scala com.gu.story.model.v1

include "contentatom.thrift"

enum ContentType {
    ARTICLE = 0,
    LIVEBLOG = 1,
    GALLERY = 2,
    INTERACTIVE = 3,
    PICTURE = 4,
    VIDEO = 5,
    CROSSWORD = 6,
    AUDIO = 7
}

struct Content {

    1: required string id

    2: required ContentType type
}

struct StoryEvent {

        1: required string id

        2: required string name

        3: required string summary

        4: required list<Content> content
}

struct Story {

        1: required string id

        2: required string name

        3: required string summary

        4: required list<StoryEvent> events

        5: required list<contentatom.Atom> atoms
}
