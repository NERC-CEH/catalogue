[#ftl]
[#assign extent=geminiHelper.getExtent(boundingBoxes)]
MAP
  INCLUDE    "/mapserver/helpers/header.inc"
  NAME       "WMS"
  STATUS     ON
  IMAGETYPE  PNG
  SIZE       2048 2048
  EXTENT     ${extent.minX?c} ${extent.minY?c} ${extent.maxX?c} ${extent.maxY?c}
  PROJECTION "init=epsg:4326" END

  WEB
    IMAGEPATH "/ms4w/tmp/ms_tmp/"
    IMAGEURL  "/ms_tmp/"
    METADATA
      "wms_onlineresource"               "${mapServerDetails.getWmsUrl(id)}"
      "wms_inspire_capabilities"         "url"
      "wms_languages"                    "eng"
      "wms_inspire_metadataurl_href"     "${uri}"
      "wms_inspire_metadataurl_format"   "application/vnd.iso.19139+xml"
      "wms_title"                        "${title}"
      "wms_abstract"                     "${description}"
      "wms_keywordlist"                  "${geminiHelper.getKeywords(descriptiveKeywords)?join(',')}"
      "wms_keywordlist_vocabulary"       "ISO"
      "wms_keywordlist_ISO_items"        "infoMapAccessService"
      "wms_contactperson"                ""
      "wms_contactorganization"          "Environmental Information Data Centre"
      "wms_contactposition"              "pointOfContact"
      "wms_addresstype"                  "Postal"
      "wms_address"                      "Centre for Ecology & Hydrology, Lancaster Environment Centre, Library Avenue, Bailrigg"
      "wms_city"                         "Lancaster"
      "wms_stateorprovince"              "Lancashire"
      "wms_postcode"                     "LA1 4AP"
      "wms_country"                      "UK"
      "wms_contactvoicetelephone"        ""
      "wms_contactfacsimiletelephone"    ""
      "wms_contactelectronicmailaddress" "eidc@ceh.ac.uk"
      "wms_accessconstraints"            "[#if accessConstraints??]${accessConstraints?first.value}[#else]None[/#if]"
      "wms_fess"                         "None"
      "wms_enable_request"               "GetCapabilities GetMap GetLegendGraphic"
      "wms_getmap_formatlist"            "image/jpeg,image/tiff,image/png"
      "wms_srs"                          "CRS:84 EPSG:4326 EPSG:3857 EPSG:3035 EPSG:27700 EPSG:4258"
      "wms_bbox_extended"                "true"
      "wms_style_name"                   "inspire_common:DEFAULT"
      "wms_style_title"                  "inspire_common:DEFAULT"
      "wms_sld_enabled"                  "false"
      END
  END

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
END