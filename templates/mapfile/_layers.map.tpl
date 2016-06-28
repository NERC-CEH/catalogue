[#ftl]
[#import "blocks.map.tpl" as blocks]
[#escape x as x?replace('"', '\\"')]
[#list doc.mapDataDefinition.data as data]
  [#list data.attributes as attr]
  [#assign prefProj=mapServerDetails.getFavouredProjection(data, epsgCode)]
  LAYER
    PROJECTION "init=epsg:${prefProj.epsgCode}" END
    PROCESSING "POLYLINE_NO_CLIP=True"
    NAME "${attr.name}"
    TYPE ${data.type} 
    STATUS ON
    METADATA
      "wms_style" "inspire_common:DEFAULT"
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
      [#if attr.buckets?has_content] [@blocks.buckets "pixel" attr.buckets/] [/#if]
    [#else]
      CLASSITEM ${attr.id}
      [#if attr.buckets?has_content] [@blocks.buckets attr.id attr.buckets/] [/#if]
    [/#if]

    [#if attr.values?has_content] [@blocks.values attr.values/] [/#if]
  END
  [/#list]
[/#list]
[/#escape]
