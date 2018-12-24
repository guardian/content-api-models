include "image.thrift"

namespace scala com.gu.tagmanagement

enum SponsorshipType {
    SPONSORED = 0,
    FOUNDATION = 1,
    PAID_CONTENT = 2
}

struct SponsorshipTargeting {
    /** only apply the sponsorship after this date */
    1: optional i64 publishedSince;

    /** only show the sponsorship for the listed editions */
    2: optional list<string> validEditions
}

struct Sponsorship {

    /** the id of the sponsorship, this is required internally but is not of use for external systems  */
    1: required i64 id;

    /** the type of the sponsorship */
    2: required SponsorshipType sponsorshipType;

    /** the name of the sponsor */
    3: required string sponsorName;

    /** the logo to display for the sponsor */
    4: required image.Image sponsorLogo;

    /** the url to link to when clicking the sponsor logo */
    5: required string sponsorLink;

    /** targeting information for the sponsorship, always show the sponsorship if this is missing */
    6: optional SponsorshipTargeting targeting;

    /** the url of a page that describes the sponsorship deal (occasionally used to describe foundation type deals
    involving multiple sponsors) */
    7: optional string aboutLink;

    /** an optional logo to be displayed on darker background media pages, if not supplied the standard logo
    will be displayed on these pages */
    8: optional image.Image highContrastSponsorLogo;

    /** The date of which the sponsorship is valid from */
    9: optional i64 validFrom;

    /** The date of which the sponsorship is valid to */
    10: optional i64 validTo;
}
