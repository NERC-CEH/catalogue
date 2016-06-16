[#ftl]
MAP
  IMAGETYPE      PNG
  EXTENT         -180 -90 180 90
  SIZE           2048 2048
  SYMBOLSET      "/mapserver/helpers/symbols.sym"

  WEB
    IMAGEPATH "/ms4w/tmp/ms_tmp/"
    IMAGEURL  "/ms_tmp/"
    METADATA
      "wms_title"          "${title}"
      "wms_onlineresource" "${mapServerDetails.getWmsUrl(id)}"
      "wms_srs"            "EPSG:29903 EPSG:27700 EPSG:4326 EPSG:3857"
      "wms_enable_request" "*"
      "wms_abstract"       "${description}"
    END
  END

  PROJECTION "init=epsg:4326" END

  LAYER
    PROJECTION "init=epsg:27700" END
    PROCESSING "POLYLINE_NO_CLIP=True"
    NAME "ukdata"
    DATA "/mapserver/data/map"
    TYPE POLYGON
    STATUS ON
    CLASS
      STYLE
        COLOR        0 255 255
        OUTLINECOLOR 255 0 0
        WIDTH        1
      END
    END
  END
END