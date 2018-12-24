include "pillar.thrift"

namespace scala com.gu.tagmanagement

enum PillarEventType {
    UPDATE = 0,
    DELETE = 1
}

struct PillarEvent {
    1: required PillarEventType eventType;

    2: required i64 pillarId;

    3: optional pillar.Pillar pillar;
}
