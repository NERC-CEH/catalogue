# Catalogue

This is the code base for the CEH gemini catalogue. The intention here is the make a code structure which is customisable for different catalogue types. EF/CEH/UKEOF

## Map of Code Base

- [java](java/README.md). Inside here is the java code which powers the catalogue. Define the object model in here which represents the content of your catalogue

- templates. Here go the freemarker templates which your object model can use for things like generating HTML pages

- [web](web/README.md). Any static or pre built content which can be hosted to help power your catalogue. (Javascripts/Images and the like)

# Map Viewer

All requests for maps go through our catalogue api as TMS coordinates (i.e. z, x, y). When a map request comes in, the catalogue api transforms the z, x, y coordinates into a wms GetMap request in the EPSG:3857 projection system. This is the projection system which is used by Google Maps style web mapping applications.

The Catalogue api will gracefully handle certain upstream mapping failures. These failures will be represented as images so that they can be displayed by the normal mapping application.

Below are the images which are displayed and there meaning:

![Legend not found](java/src/main/resources/legend-not-found.png) Displayed when a Legend image is requested but one has not been specified in the GetCapabilities

![Upstream Failure](java/src/main/resources/proxy-failure.png) The call to the server failed for some unspecified reason, this may be because the connection failed.

![Invalid response](java/src/main/resources/proxy-invalid-response.png) The upstream service returned some content, but it was not in the format which was expected. It maybe that the upstream service replied with an error message rather than an image.

|[Invalid Resource](java/src/main/resources/proxy-invalid-resource.png) The wms get capabilities returned a malformed reference to either a GetLegend or GetMap url. This can happen if you are using a buggy web map server or an corrupt external get capabilities.