namespace scala com.gu.contentapi.client.model.v1

struct CapiDateTime {

    /*
     * Date times are represented as i64 - epoch millis
     */
    1: required i64 dateTime

    /*
     * Also represent as a string (yyyy-MM-dd`T`HH:mm:ss.SSSZZ) in order to preserve timezone info.
     *
     * Note: this field makes the i64 representation redundant.
     */
    2: required string iso8601

}

