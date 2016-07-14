[#ftl]
[#import "blocks.map.tpl" as blocks]
[#escape x as x?replace('"', '\\"')]
[#list doc.mapDataDefinition.data as data]
  [#list data.attributes as attr]
  [#assign prefProj=mapServerDetails.getFavouredProjection(data, epsgCode)]
  [#assign dataType=(attr.type.name())!"TEXT"]
  LAYER
    PROJECTION "init=epsg:${prefProj.epsgCode}" END
    PROCESSING "POLYLINE_NO_CLIP=True"
    NAME "${attr.name}"
    TYPE ${data.type} 
    TEMPLATE "dummy"
    STATUS ON
    METADATA
      "wms_title" "${attr.label!attr.name}"
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
      CONNECTION "/mapserver/data/${prefProj.path}"
      DATA "${data.layer}"
    [#else]
      DATA "${prefProj.path}"
    [/#if]
    
    [#--
    If we are looking at a RASTER dataset then we will consider the id attribute to 
    be the band in the raster data set to focus on. In any other data type, we will
    use the id to refer to a CLASSITEM
    --]
    [#if data.type?lower_case == "raster"]
      PROCESSING "BANDS=${attr.id}"
      [#if attr.buckets?has_content] 
        [#--
        If the specified buckets are closed (do not have open ends) then we can tell 
        mapserver to classify the raster in to buckets of equal size before our bucket 
        expressions are evaluated.

        [More info](http://mapserver.org/input/raster.html#classifying-non-8bit-rasters)
        --]
        [#assign hints=mapServerDetails.getScaledBuckets(attr.buckets)!]
        [#if hints?has_content]
          PROCESSING "SCALE=${hints.min?c},${hints.max?c}"
          PROCESSING "SCALE_BUCKETS=${hints.buckets?c}"
        [/#if]
        [@blocks.buckets "pixel" attr.buckets/] 
      [/#if]
    [#else]
      CLASSITEM "${attr.id}"
      [#if attr.buckets?has_content] [@blocks.buckets attr.id attr.buckets/] [/#if]
    [/#if]

    [#if attr.values?has_content] [@blocks.values attr.id dataType attr.values/] [/#if]
  END
  [/#list]
[/#list]
[/#escape]
