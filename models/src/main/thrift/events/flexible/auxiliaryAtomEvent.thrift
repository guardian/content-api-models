namespace scala com.gu.auxiliaryatom.model.auxiliaryatomevent.v1
#@namespace typescript _at_guardian.content_api_models.auxiliaryatomevent.v1

enum EventType {
    ADD = 0,
    REMOVE = 1
}

struct AuxiliaryAtom {
    1: required string atomId
    2: required string atomType
}

struct AuxiliaryAtomEvent {
    1: required string contentId
    2: required EventType eventType
    3: required list<AuxiliaryAtom> auxiliaryAtom
}