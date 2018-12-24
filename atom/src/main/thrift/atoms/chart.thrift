namespace * contentatom.chart
namespace java com.gu.contentatom.thrift.atom.chart
#@namespace scala com.gu.contentatom.thrift.atom.chart

enum ChartType {
    BAR = 0
}

struct Range {
    1: required double min
    2: required double max
}

struct DisplaySettings {
    1: required bool showHeadline = true
    2: required bool showSource = true
    3: optional bool showStandfirst
    4: optional bool showLegend
}

struct Furniture {
    1: required string headline
    2: required string source
    3: optional string standfirst
}

enum RowType {
    STRING = 0,
    DATE = 1
}

struct TabularData {
    1: required RowType rowHeadersType
    2: required list<string> columnHeaders
    3: required list<string> rowHeaders
    4: required list<list<double>> rowData
}

struct SeriesColour {
    1: required i32 index // a column or row
    2: required string hexCode
}

struct Axis {
    1: required list<double> scale
    2: required Range range
}

struct ChartAtom {
    1: required ChartType chartType
    2: required Furniture furniture
    3: required TabularData tabularData
    4: required list<SeriesColour> seriesColour
    5: required DisplaySettings displaySettings
    6: optional list<i32> hiddenColumns
    7: optional list<i32> hiddenRows
    8: optional Axis xAxis
    9: optional Axis yAxis
}
