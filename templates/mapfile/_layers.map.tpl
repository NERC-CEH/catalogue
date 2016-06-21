[#ftl]
[#import "blocks.map.tpl" as blocks]
[#list mapDataDefinition.data as data]
  [#list data.attributes as attr]
  LAYER
    PROJECTION "init=epsg:${data.epsgCode}" END
    PROCESSING "POLYLINE_NO_CLIP=True"
    NAME "${attr.name}"
    DATA "${data.path}"
    TYPE ${data.type} 
    STATUS ON
    METADATA
      "wms_style" "inspire_common:DEFAULT"
    END
    
    [#--
    If we are looking at a RASTER dataset then we will consider the id attribute to 
    be the band in the raster data set to focus on. In any other data type, we will
    use the id to refer to a CLASSITEM
    --]
    [#if data.type?lower_case == "raster"]
      PROCESSING "BANDS=${attr.id}"
      [#if attr.buckets?has_content] [@blocks.buckets "pixel" attr.buckets/] [/#if]
    [#else]
      CLASSITEM ${attr.id}
      [#if attr.buckets?has_content] [@blocks.buckets attr.id attr.buckets/] [/#if]
    [/#if]

    [#if attr.values?has_content] [@blocks.values attr.values/] [/#if]
  END
  [/#list]
[/#list]
