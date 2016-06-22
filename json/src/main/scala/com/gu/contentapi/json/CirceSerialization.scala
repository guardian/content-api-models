package com.gu.contentapi.json

import cats.data.Xor
import com.gu.contentatom.thrift.{Atom, AtomData, AtomType, ContentChangeDetails}
import com.gu.contentatom.thrift.atom.media.{MediaAtom, Platform, AssetType => MediaAtomAssetType}
import com.gu.contentatom.thrift.atom.quiz.QuizAtom
import com.gu.contentapi.circe.CirceScroogeHelper._
import com.gu.contentapi.client.model.v1.Atoms
import io.circe.{Decoder, DecodingFailure, HCursor}
import io.circe.generic.auto._


object CirceSerialization {

  type Result[A] = Xor[DecodingFailure, A]

  implicit val atomTypeDecoder = new Decoder[AtomType] {
    final def apply(c: HCursor): Result[AtomType] = c.focus.asString match {
      case Some(value) => Xor.right(AtomType.valueOf(value).getOrElse(AtomType.EnumUnknownAtomType(-1)))
      case _ => Xor.left(DecodingFailure("AtomType", c.history))
    }
  }

  implicit val mediaAtomAssetTypeDecoder = new Decoder[MediaAtomAssetType] {
    final def apply(c: HCursor): Result[MediaAtomAssetType] = c.focus.asString match {
      case Some(value) => Xor.right(MediaAtomAssetType.valueOf(value).getOrElse(MediaAtomAssetType.EnumUnknownAssetType(-1)))
      case _ => Xor.left(DecodingFailure("MediaAtomAssetType", c.history))
    }
  }

  implicit val platformTypeDecoder = new Decoder[Platform] {
    final def apply(c: HCursor): Result[Platform] = c.focus.asString match {
      case Some(value) => Xor.right(Platform.valueOf(value).getOrElse(Platform.EnumUnknownPlatform(-1)))
      case _ => Xor.left(DecodingFailure("Platform", c.history))
    }
  }

  implicit val decodeUnknownOpt: Decoder[AtomData.UnknownUnionField] =
    Decoder.instance(c =>
      Xor.left(DecodingFailure("AtomData.UnknownUnionField", c.history))
    )

  implicit val atomDecoder: Decoder[Atom] = Decoder.instance(getAtom)

  implicit val atomsDecoder: Decoder[Atoms] = Decoder.instance(getAtoms)

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
      case None => Xor.right(None)  //ignore unrecognised atom types
    }
  }

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
}
