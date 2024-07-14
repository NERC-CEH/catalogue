[#ftl]
[#import "blocks.map.ftl" as blocks]
[#list doc.mapDataDefinition.data as data]
  [#if data.attributes?has_content]
    [#list data.attributes as attr]
    [@blocks.layer data attr.name attr.label!attr.name]
      [#assign dataType=(attr.type.name())!"TEXT"]
      [#--
      If we are looking at a RASTER dataset then we will consider the id attribute to
      be the band in the raster data set to focus on. In any other data type, we will
      use the id to refer to a CLASSITEM
      --]
      [#if data.type?lower_case == "raster"]
        PROCESSING "BANDS=${attr.id}"
        PROCESSING "RESAMPLE=NEAREST"
        [#if attr.buckets?has_content]
          [#--
          If the specified buckets are closed (do not have open ends) then we can tell
          mapserver to classify the raster in to buckets of equal size before our bucket
          expressions are evaluated.

          [More info](http://mapserver.org/input/raster.html#classifying-non-8bit-rasters)
          --]
          [#--
          If bytetype is empty then default to false on the assumption that the source geotif is not byte and will require scaling
          --]
          [#if !((data.bytetype)!false)]
            [#assign hints=mapServerDetails.getScaledBuckets(attr.buckets)!]
            [#if hints?has_content]
              PROCESSING "SCALE=${hints.min?c},${hints.max?c}"
              PROCESSING "SCALE_BUCKETS=${hints.buckets?c}"
            [/#if]
          [/#if]
          [@blocks.buckets "pixel" attr.buckets/]
        [/#if]
      [#else]
        CLASSITEM "${attr.id}"
        [#if attr.buckets?has_content] [@blocks.buckets attr.id attr.buckets/] [/#if]
      [/#if]

      [#if attr.values?has_content] [@blocks.values attr.id dataType attr.values/] [/#if]
    [/@blocks.layer]
    [/#list]
  [/#if]

  [#if data.features?has_content]
    [@blocks.layer data data.features.name data.features.name!data.features.label]
      CLASS
        NAME "${data.features.label}"
        [@blocks.style data.features.style /]
      END
    [/@blocks.layer]
  [/#if]
[/#list]
