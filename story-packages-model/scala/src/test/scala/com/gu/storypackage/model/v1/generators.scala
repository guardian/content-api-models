package com.gu.storypackage.model

import org.scalacheck.{Arbitrary, Gen}

package object v1 {
    
    import Arbitrary.arbitrary

    implicit val arbEventType: Arbitrary[EventType] =
        Arbitrary(Gen.oneOf(EventType.Update, EventType.Delete))

    implicit val arbArticleType: Arbitrary[ArticleType] =
        Arbitrary(Gen.oneOf(ArticleType.Article, ArticleType.Snap))

    implicit val arbGroup: Arbitrary[Group] =
        Arbitrary(Gen.oneOf(Group.Included, Group.Linked))

    implicit val arbArticle: Arbitrary[Article] = Arbitrary {
        for {
            id <- arbitrary[String]
            articleType <- arbitrary[ArticleType]
            grou <- arbitrary[Group]
            headline <- arbitrary[Option[String]]
            href <- arbitrary[Option[String]]
            trailText <- arbitrary[Option[String]]
            imageSrc <- arbitrary[Option[String]]
            isBoosted <- arbitrary[Option[Boolean]]
            imageHide <- arbitrary[Option[Boolean]]
            showMainVideo <- arbitrary[Option[Boolean]]
            showKickerTag <- arbitrary[Option[Boolean]]
            showKickerSection <- arbitrary[Option[Boolean]]
            byline <- arbitrary[Option[String]]
            customKicker <- arbitrary[Option[String]]
            showBoostedHeadline <- arbitrary[Option[Boolean]]
            showQuotedHeadline <- arbitrary[Option[Boolean]]
        } yield Article(id, articleType, grou, headline, href, trailText, imageSrc, isBoosted, imageHide, showMainVideo, showKickerTag, showKickerSection, byline, customKicker, showBoostedHeadline, showQuotedHeadline)
    }

    implicit val arbEvent: Arbitrary[Event] = Arbitrary {
        for {
            eventType <- arbitrary[EventType]
            packageId <- arbitrary[String]        
            packageName <- arbitrary[String]        
            lastModified <- arbitrary[String]        
            articles <- arbitrary[List[Article]]        
        } yield Event(eventType, packageId, packageName, lastModified, articles)
    }
}