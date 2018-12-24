namespace java com.gu.contententity.thrift.entity.game

include "../shared.thrift"

struct Game {
  1: required string title
  2: optional string publisher
  3: required list<string> platforms
  4: optional shared.Price price
  5: optional i32 pegiRating
  6: required list<string> genre
}