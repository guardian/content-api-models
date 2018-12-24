namespace java com.gu.contentatom.thrift
#@namespace scala com.gu.contentatom.thrift

include "atoms/quiz.thrift"
include "atoms/media.thrift"
include "atoms/explainer.thrift"
include "atoms/cta.thrift"
include "atoms/guide.thrift"
include "atoms/interactive.thrift"
include "atoms/profile.thrift"
include "atoms/qanda.thrift"
include "atoms/review.thrift"
include "atoms/recipe.thrift"
include "atoms/storyquestions.thrift"
include "atoms/timeline.thrift"
include "atoms/commonsdivision.thrift"
include "atoms/chart.thrift"
include "shared.thrift"

typedef string ContentAtomID

enum AtomType {
  QUIZ = 0,
//VIEWPOINTS = 1, DEPRECATED
  MEDIA = 2,
  EXPLAINER = 3,
  CTA = 4,
  INTERACTIVE = 5,
  REVIEW = 6,
  RECIPE = 7,
  STORYQUESTIONS = 8,
  QANDA = 9,
  PROFILE = 10,
  GUIDE = 11,
  TIMELINE = 12,
  COMMONSDIVISION = 13,
  CHART = 14
}

union AtomData {
  1: quiz.QuizAtom quiz
//2: viewpoints.ViewpointsAtom viewpoints DEPRECATED
  3: media.MediaAtom media
  4: explainer.ExplainerAtom explainer
  5: cta.CTAAtom cta
  6: interactive.InteractiveAtom interactive
  7: review.ReviewAtom review
  8: recipe.RecipeAtom recipe
  9: storyquestions.StoryQuestionsAtom storyquestions
  10: qanda.QAndAAtom qanda
  11: guide.GuideAtom guide
  12: profile.ProfileAtom profile
  13: timeline.TimelineAtom timeline
  14: commonsdivision.CommonsDivision commonsDivision
  15: chart.ChartAtom chart
}

struct ContentChangeDetails {
  /** the latest change to the content atom */
  1: optional shared.ChangeRecord lastModified

  /** the atom creation event */
  2: optional shared.ChangeRecord created

  /** the atom publication event (if published) */
  3: optional shared.ChangeRecord published
  /**
  * the revision number of the content.
  *
  * This value is incremented whenever content is written to the database and can be used to
  * ensure message ordering.
  */
  4: required i64 revision

  /** the atom taken down event */
  5: optional shared.ChangeRecord takenDown

  /** scheduled launch date */
  6: optional shared.ChangeRecord scheduledLaunch

  /** embargo date */
  7: optional shared.ChangeRecord embargo

//this has been deprecated due to incorrect naming and should not be used
//8: optional shared.ChangeRecord expiryDate

  /** expiry date */
  9: optional shared.ChangeRecord expiry
}

struct Flags {
//1: optional bool suppressFurniture DEPRECATED
  2: optional bool legallySensitive
  3: optional bool blockAds
  4: optional bool sensitive
}

struct Atom {
  1: required ContentAtomID id
  2: required AtomType atomType
  3: required list<string> labels // required, but may be empty
  4: required string defaultHtml
  5: required AtomData data       // the atom payload
  6: required ContentChangeDetails contentChangeDetails
  7: optional Flags flags
  8: optional string title
  9: optional list<string> commissioningDesks = []
 }

enum EventType {
  UPDATE = 0,
  TAKEDOWN = 1
}

struct ContentAtomEvent {
  1: required Atom atom
  2: required EventType eventType
  3: required shared.DateTime eventCreationTime
}
