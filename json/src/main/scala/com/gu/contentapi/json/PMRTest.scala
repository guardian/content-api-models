package stelo

// import io.circe._
// import io.circe.parser.parse
// import scala.io.Source
import com.gu.contentatom.thrift.AtomType
import com.gu.contentatom.thrift.atom
// import com.gu.contentatom.thrift.atom.cta.CTAAtom
// import com.gu.contentapi.circe.CirceScroogeMacros._

object PMRTest {

  trait MapsTo[+A, B]

  implicit object quizMapsTo extends MapsTo[AtomType.Quiz.type, atom.quiz.QuizAtom]
  implicit object mediaMapsTo extends MapsTo[AtomType.Media.type, atom.media.MediaAtom]
  implicit object explainerMapsTo extends MapsTo[AtomType.Explainer.type, atom.explainer.ExplainerAtom]
  implicit object ctaMapsTo extends MapsTo[AtomType.Cta.type, atom.cta.CTAAtom]

//implicit object errorMapsTo extends MapsTo[AtomType, atom.quiz.QuizAtom]

  // trait TypeMapper[T <: AtomType, D]

  // def tmap[T <: AtomType, D] = new TypeMapper[T, D] {}

  // implicit val tm = tmap[AtomType.Cta.type, CTAAtom]

  // val js = parse("""{"url":"http://www.google.com","trackingCode":"ABCDEF"}""").getOrElse(Json.Null)

  // def getIt[T <: AtomType, D : Decoder](atomType: T)(implicit arg1: TypeMapper[T, D]): Option[D] = {
  //   js.as[D].toOption
  //   }

  // lazy val cta = js.as[CTAAtom]

}
