include "entities/game.thrift"
include "entities/restaurant.thrift"
include "entities/film.thrift"
include "../shared.thrift"

namespace * contentatom.review
namespace java com.gu.contentatom.thrift.atom.review
#@namespace scala com.gu.contentatom.thrift.atom.review

enum ReviewType {
  RESTAURANT = 1
  GAME = 2
  FILM = 3
}

struct Rating {
  1: required i16 maxRating
  2: required i16 actualRating
  3: required i16 minRating
}

struct ReviewAtom {
  1: required ReviewType reviewType
  2: required string reviewer
  3: required Rating rating
  4: required string reviewSnippet
  5: required string entityId
  6: optional restaurant.Restaurant restaurant
  7: optional game.Game game
  8: optional film.Film film
  9: optional string sourceArticleId
  10: optional list<shared.Image> images
}
