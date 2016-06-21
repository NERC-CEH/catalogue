[#ftl]
[#import "blocks.map.tpl" as blocks]
[#list mapDataDefinition.data as data]  
[#if data.type == "shapefile"]
  [#list data.attributes as attr]
  LAYER
    PROJECTION "init=epsg:${data.epsgCode}" END
    PROCESSING "POLYLINE_NO_CLIP=True"
    NAME "${attr.name}"
    DATA "${data.path}"
    CLASSITEM ${attr.id}
    TYPE POLYGON 
    STATUS ON
    METADATA
      "wms_style" "inspire_common:DEFAULT"
    END
    [#if attr.buckets?has_content]
      [@blocks.buckets attr.id attr.buckets/]
    [/#if]
    [#if attr.values?has_content]
      [@blocks.values attr.values/]
    [/#if]
  END
  [/#list]
[/#if]  
[/#list]
