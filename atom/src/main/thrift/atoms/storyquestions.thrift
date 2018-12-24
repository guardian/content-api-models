namespace * contentatom.storyquestions
namespace java com.gu.contentatom.thrift.atom.storyquestions
#@namespace scala com.gu.contentatom.thrift.atom.storyquestions

include "../shared.thrift"

/*
 * Determines what the questions are linked to. This is fundamentally to work around the fact
 * that we do not have the concept of a story yet.
 */
enum RelatedStoryLinkType {
  TAG = 0,
  STORY = 1,
}

enum AnswerType {
  CONTENT = 0,
  ATOM = 1
}

struct Answer {
  1: required string answerId
  2: required AnswerType answerType
}

struct Question {
  1: required string questionId
  2: required string questionText
  3: optional list<Answer> answers = []
}

struct QuestionSet {
  1: required string questionSetId
  2: required string questionSetTitle
  3: required list<Question> questions
}


struct StoryQuestionsAtom {
  1: required string relatedStoryId
  2: required RelatedStoryLinkType relatedStoryLinkType
  3: required string title
  4: optional list<QuestionSet> editorialQuestions
  5: optional list<QuestionSet> userQuestions
  6: optional shared.NotificationProviders notifications
  7: optional shared.DateTime closeDate
}
