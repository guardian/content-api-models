namespace * contentatom.quiz
namespace java com.gu.contentatom.thrift.atom.quiz
# for some reason scrooge (the version we're using at least) overrides 'java'
# with '*', so we need to add the scala namespace. Apache Thrift will reject
# this so they have allowed this special format which appears as a comment to
# Thrift.
#@namespace scala com.gu.contentatom.thrift.atom.quiz

include "../shared.thrift"

struct ResultGroup {
  1: required string title
  2: required string share
  3: required i16 minScore
  4: required string id
}

struct Asset {
  1: required string type
  2: required shared.OpaqueJson data
}

struct Answer {
  1: required string answerText
  2: required list<Asset> assets
  3: required i16 weight
  4: optional string revealText
  5: required string id
  6: optional list<string> bucket # A list of bucket ids
}

struct ResultBucket {
  1: optional list<Asset> assets
  2: required string description
  3: required string title
  4: required string share
  5: required string id
}

struct ResultBuckets {
  1: required list<ResultBucket> buckets
}

struct Question {
  1: required string questionText
  2: required list<Asset> assets
  3: required list<Answer> answers
  4: required string id
}

struct ResultGroups {
  1: required list<ResultGroup> groups
}

struct QuizContent {
  1: required list<Question> questions
  2: optional ResultGroups resultGroups
  3: optional ResultBuckets resultBuckets
}

struct QuizAtom {
  // do we need to store the ID, seeing as it is replicated(?) in the
  // content-atom wrapping?
  1: required string id
  2: required string title
  6: required bool revealAtEnd
  7: required bool published
  8: required string quizType
  9: optional i16 defaultColumns
  10: required QuizContent content
}
