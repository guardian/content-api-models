namespace scala com.gu.contentapi.client.model.v1

include "CapiDateTime.thrift"

enum SponsorshipType {
    SPONSORED = 0,
    FOUNDATION = 1,
    PAID_CONTENT = 2
}

struct SponsorshipTargeting {
    1: optional CapiDateTime.CapiDateTime publishedSince

    2: optional list<string> validEditions
}

struct SponsorshipLogoDimensions {

    1: required i32 width

    2: required i32 height

}

struct Sponsorship {

    1: required SponsorshipType sponsorshipType

    2: required string sponsorName

    3: required string sponsorLogo

    4: required string sponsorLink

    5: optional SponsorshipTargeting targeting

    6: optional string aboutLink

    7: optional SponsorshipLogoDimensions sponsorLogoDimensions

    8: optional string highContrastSponsorLogo

    9: optional SponsorshipLogoDimensions highContrastSponsorLogoDimensions

    10: optional CapiDateTime.CapiDateTime validFrom

    11: optional CapiDateTime.CapiDateTime validTo

}
