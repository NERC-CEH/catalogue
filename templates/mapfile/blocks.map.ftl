[#ftl output_format="plainText"]
[#--
  Generates a layer block for the given datasource
  $data data source
  $name the layer name which will be used when called in WMS requests
  $title of the layer
--]
[#macro layer data name title]
  [#assign prefProj=mapServerDetails.getFavouredProjection(data, epsgCode)]
  [#assign isLine=(data.type?upper_case == "LINE")]
  LAYER
    PROJECTION "init=epsg:${prefProj.epsgCode}" END
    PROCESSING "POLYLINE_NO_CLIP=True"
    NAME "${name}"
    TYPE ${data.type}
    TEMPLATE "dummy"
    STATUS ON
    [#-- Sets the map scale the line width starts to change from --]
    [#if isLine]SYMBOLSCALEDENOM 25000[/#if]
    METADATA
      "wms_title" "${title}"
      "wms_style" "inspire_common:DEFAULT"
      "gml_include_items" "all"
      "wms_include_items" "all"
    END

    [#--
    Esri File GeoDatabase support

    Determine if we should use an OGR connection type to access the source data. If
    a layer attribute is present then we will do, otherwise just use a standard
    mapserver DATA block.
    --]
    [#if data.layer?has_content]
      CONNECTIONTYPE OGR
      CONNECTION "/data/${prefProj.path}"
      DATA "${data.layer}"
    [#else]
      DATA "${prefProj.path}"
    [/#if]

    [#nested]
  END
[/#macro]

[#--
  Generates the correct MapServer expression value for the supplied bucket
  and the given attribute value.

  $attr which the bucket is performed on
  $bucket contains the range of the bucket values
--]
[#macro bucketExpr attr bucket]
  [#compress]
  [#if bucket.min?? && bucket.max??]
    (${bucket.min?c} < [${attr}] AND [${attr}] <= ${bucket.max?c})
  [#elseif bucket.min??]
    (${bucket.min?c} < [${attr}])
  [#elseif bucket.max??]
    ([${attr}] <= ${bucket.max?c})
  [/#if]
  [/#compress]
[/#macro]

[#--
  Generate a value expression for the given value. This Macro will vary
  the type of expression which is written based upon the attributes type
  (number/text).
--]
[#macro valueExpr attr type value]
  [#compress]
  [#if type == "TEXT"]
    "${value.setting}"
  [#elseif type == "NUMBER"]
    ([${attr}] = ${value.setting})
  [/#if]
  [/#compress]
[/#macro]

[#--
  Generate a list of class blocks for the supplied buckets
--]
[#macro buckets attr buckets lineMode=false]
  [#list buckets as bucket]
    CLASS
      NAME "${bucket.label}"
      EXPRESSION [@bucketExpr attr bucket/]
      [@style bucket.style lineMode/]
    END
  [/#list]
[/#macro]

[#--
  Generate a list of class blocks for the supplied value settings
--]
[#macro values attr type values lineMode=false]
  [#list values as value]
    CLASS
      NAME "${value.label}"
      EXPRESSION [@valueExpr attr type value/]
      [@style value.style lineMode/]
    END
  [/#list]
[/#macro]

[#--
  Generate a style block for the current colour data.
  LineMode will be true when data type Line is selected in the editor, min/max size controls the width of the lines
  so they don't scale too big or small. Width is the base value to scale from
--]
[#macro style data lineMode=false]
  STYLE
    COLOR "${data.colour}"
    [#if data.symbol?has_content]
      SYMBOL "${data.symbol}"
      SIZE 6
    [/#if]
    [#if lineMode]
      WIDTH 1.0
      MINSIZE 1
      MAXSIZE 6
      LINECAP "round"
      LINEJOIN "round"
    [/#if]
  END
[/#macro]
