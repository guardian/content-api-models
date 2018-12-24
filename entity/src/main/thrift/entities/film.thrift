namespace java com.gu.contententity.thrift.entity.film

include "./person.thrift"

struct Film {
  1: required string title
  2: required i16 year
  3: required string imdbId
  4: required list<person.Person> directors
  5: required list<person.Person> actors
  6: required list<string> genre
}
