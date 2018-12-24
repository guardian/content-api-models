include "tag.thrift"

namespace scala com.gu.tagmanagement

struct TagReindexBatch {
    1: required list<tag.Tag> tags;
}
