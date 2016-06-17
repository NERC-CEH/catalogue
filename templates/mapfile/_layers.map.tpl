[#ftl]
[#list mapServiceDefinition.layers as layer]    
LAYER
  PROJECTION "init=epsg:${layer.epsgCode}" END
  PROCESSING "POLYLINE_NO_CLIP=True"
  NAME "${layer.name}"
  DATA "${layer.data}"
  TYPE ${layer.type}
  STATUS ON
  METADATA
    "wms_style" "inspire_common:DEFAULT"
  END
  [#list layer.classes as class]
  CLASS
    NAME       "${class.name}"
    EXPRESSION (${class.expression})
    STYLE
      COLOR ${class.style.colour}
    END
  END
  [/#list]
END
[/#list]
