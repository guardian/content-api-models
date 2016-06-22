package com.gu.contentapi.json

import cats.data.Xor
import com.gu.contentatom.thrift.{Atom, AtomData, AtomType, ContentChangeDetails}
import com.gu.contentatom.thrift.atom.media.MediaAtom
import com.gu.contentatom.thrift.atom.quiz.QuizAtom

import com.gu.contentapi.circe.CirceScroogeMacros._
import com.gu.contentapi.client.model.v1.Atoms
import io.circe.{Decoder, DecodingFailure, HCursor}

import com.gu.contentapi.client.model.v1._
import io.circe._
import io.circe.generic.auto._
import org.joda.time.format.ISODateTimeFormat


object CirceSerialization {

  type Result[A] = Xor[DecodingFailure, A]

  implicit val dateTimeDecoder = new Decoder[CapiDateTime] {
    final def apply(c: HCursor): Result[CapiDateTime] = c.focus.asString match {
      case Some(value) =>
        val dateTime = ISODateTimeFormat.dateOptionalTimeParser().withOffsetParsed().parseDateTime(value)
        Xor.right(CapiDateTime.apply(dateTime.getMillis, dateTime.toString(ISODateTimeFormat.dateTime())))
      case _ => Xor.left(DecodingFailure("CapiDateTime", c.history))
    }
  }

  implicit val atomDecoder: Decoder[Atom] = Decoder.instance(AtomDeserialization.getAtom)

  implicit val atomsDecoder: Decoder[Atoms] = Decoder.instance(AtomDeserialization.getAtoms)

  object AtomDeserialization {

    implicit val decodeUnknownOpt: Decoder[AtomData.UnknownUnionField] =
      Decoder.instance(c =>
        Xor.left(DecodingFailure("AtomData.UnknownUnionField", c.history))
      )

    def getAtom(c: HCursor): Result[Atom] = {
      for {
        atomType <- c.downField("atomType").as[AtomType]
        atomData <- getAtomData(c, atomType)
        atom <- getAtom(c, atomData)
      } yield atom
    }

    def getAtoms(c: HCursor): Result[Atoms] = {
      for {
        quizzes <- getAtoms(c, AtomType.Quiz)
        media <- getAtoms(c, AtomType.Media)
      } yield {
        Atoms(quizzes = quizzes, media = media)
      }
    }

    private def getAtom(c: HCursor, atomData: AtomData): Result[Atom] = {
      for {
        id <- c.downField("id").as[String]
        atomType <- c.downField("atomType").as[AtomType]
        labels <- c.downField("labels").as[Seq[String]]
        defaultHtml <- c.downField("defaultHtml").as[String]
        change <- c.downField("contentChangeDetails").as[ContentChangeDetails]
      } yield {
        Atom(id, atomType, labels, defaultHtml, atomData, change)
      }
    }

    private def getAtomData(c: HCursor, atomType: AtomType): Result[AtomData] = {
      atomType match {
        case AtomType.Quiz => c.downField("data").downField("quiz").as[QuizAtom].map(json => AtomData.Quiz(json))
        case AtomType.Media => c.downField("data").downField("media").as[MediaAtom].map(json => AtomData.Media(json))
        case _ => Xor.left(DecodingFailure("AtomData", c.history))
      }
    }

    private def getAtomTypeFieldName(atomType: AtomType): Option[String] = {
      atomType match {
        case AtomType.Quiz => Some("quizzes")
        case AtomType.Media => Some("media")
        case _ => None
      }
    }

    private def getAtoms(c: HCursor, atomType: AtomType): Result[Option[Seq[Atom]]] = {
      getAtomTypeFieldName(atomType) match {
        case Some(fieldName) => c.get[Option[Seq[Atom]]](fieldName)
        case None => Xor.right(None) //ignore unrecognised atom types
      }
    }
  }
}
