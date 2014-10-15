# Catalogue

This is the code base for the CEH gemini catalogue. The intention here is the make a code structure which is customisable for different catalogue types. EF/CEH/UKEOF

## Map of Code Base

- [java](java/README.md). Inside here is the java code which powers the catalogue. Define the object model in here which represents the content of your catalogue

- templates. Here go the freemarker templates which your object model can use for things like generating HTML pages

- [web](web/README.md). Any static or pre built content which can be hosted to help power your catalogue. (Javascripts/Images and the like)

# MapProxy

The catalogues map viewer uses MapProxy to ensure that upstream wms services specified in Metadata Records respond quickly and are previewable in the desired EPSG:3587 projection system.

1. Creating the wms proxy file

  All requests for maps go through our catalogue api. When a map request comes in, a corresponding mapproxy yaml configuration file will be created (if not already created before) for the wms to be proxied. This yaml file will be saved in the directory which MapProxy is hosting, as such it will automatically be registered.

2. Requesting a tile

  The api will then go ahead and request the tile (which the client requested) from the newly created map proxy service. This will be transparently proxied back to the client. MapProxy will cache this tile so subsequent requests will respond without interaction from the upstream wms.