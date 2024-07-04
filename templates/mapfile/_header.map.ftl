[#ftl]
[#assign extent=geminiHelper.getExtent(doc)]
EXTENT    ${extent.minX?c} ${extent.minY?c} ${extent.maxX?c} ${extent.maxY?c}
IMAGETYPE PNG
SIZE      2048 2048
SYMBOLSET "/mapserver/helpers/symbols.sym"
FONTSET   "/mapserver/helpers/fonts/fonts.list"
SHAPEPATH "/mapserver/data/"
NAME      "WMS"
STATUS    ON
PROJECTION
  "init=epsg:4326"
END
LEGEND
  KEYSPACING 5 7
  KEYSIZE 20 15
  LABEL
    TYPE TRUETYPE
    FONT opensans-semibold
    SIZE 10
    POSITION AUTO
    PARTIALS FALSE
  END
END
