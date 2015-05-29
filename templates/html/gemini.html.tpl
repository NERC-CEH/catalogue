<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<#assign authors       = _.filter(responsibleParties, _.isAuthor) >
<#assign otherContacts = _.reject(responsibleParties, _.isAuthor) >
<@skeleton.master title=title rdf="${uri}?format=rdf">
  <div id="metadata">
    <div class="container">

      <#include "metadata/_title.html.tpl">
      <#include "metadata/_notCurrent.html.tpl">
      <#include "metadata/_authorsTop.html.tpl">
      <#include "metadata/_description.html.tpl">
      <#include "metadata/_dates.html.tpl">

      <div class="row">
        <div class="col-sm-4 col-xs-12 pull-right">
          <#include "metadata/_distribution.html.tpl">
          <#include "metadata/_children.html.tpl">
          <#include "metadata/_related.html.tpl">
          <#include "metadata/_actions.html.tpl">
        </div>
        <div class="col-sm-8 col-xs-12">
          <#include "metadata/_extent.html.tpl">
          <#include "metadata/_onlineResources.html.tpl">
          <#include "metadata/_quality.html.tpl">

          <#include "metadata/_authors.html.tpl">
          <#include "metadata/_otherContacts.html.tpl">
          <#include "metadata/_spatial.html.tpl">
          <#include "metadata/_keywords.html.tpl">
          <#include "metadata/_uris.html.tpl">
        </div>
      </div>

      <#include "metadata/_metadata.html.tpl">

    </div>
    <div id="footer">
        <#include "metadata/_footer.html.tpl">
    </div>
    <script>
        var gemini = {alternateTitles:["alt0","alt1"], uri:"http://rod.ceh.ac.uk:8080/id/fb495d1b-80a3-416b-8dc7-c85ed22ed1e3",id:"fb495d1b-80a3-416b-8dc7-c85ed22ed1e3","title":"Natural radionuclide concentrations in soil, water and sediments in England and Wales survey maps","description":"This is a view service of 9 layers of estimates of radionuclides activity concentrations from soil, stream water and stream sediment. \n40K, 238U and 232Th series radionuclides were derived from total K, U and Th concentrations obtained mainly from the ongoing geochemical survey of the United Kingdom (G-BASE), conducted by the British Geological Survey. The geochemical survey data are currently incomplete for England and Wales, but almost complete coverage was obtained for K in stream sediments by using the Wolfson Atlas data for southern England.","browseGraphicUrl":"https://gateway.ceh.ac.uk:443/smartEditor/preview/fb495d1b-80a3-416b-8dc7-c85ed22ed1e3.png","metadataStandardName":"NERC profile of ISO19115:2003","metadataStandardVersion":"1.0","coupledResources":["CEH:EIDC:#1271758286281"],"accessConstraints":["otherRestrictions"],"resourceType":{"value":"service"},"useLimitations":[{"value":"Terms and conditions apply (http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/grant-of-licence-for-web-mapping-services/plain)"}],"otherConstraints":[{"value":"Terms and conditions apply (http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/grant-of-licence-for-web-mapping-services/plain)"},{"value":"Terms and conditions apply","uri":"http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/grant-of-licence-for-web-mapping-services/plain"}],"distributionFormats":[{"name":"various","version":"various"}],"descriptiveKeywords":[{"keywords":[{"value":"Human health and safety"},{"value":"Soil"},{"value":"radioecology"},{"value":"radioactive substance"}],"type":"theme"},{"keywords":[{"value":"Soil"},{"value":"sediment"},{"value":"water"}],"type":"stratum"},{"keywords":[{"value":"1984"},{"value":"1985"},{"value":"1986"},{"value":"1987"},{"value":"1988"},{"value":"1989"},{"value":"1990"},{"value":"1991"},{"value":"1992"},{"value":"1993"},{"value":"1994"},{"value":"1995"},{"value":"1996"},{"value":"1997"},{"value":"1998"},{"value":"1999"},{"value":"2000"},{"value":"2001"},{"value":"2002"},{"value":"2003"},{"value":"2004"},{"value":"2005"},{"value":"2006"}],"type":"temporal"},{"keywords":[{"value":"infoMapAccessService"}],"type":"discipline"},{"keywords":[{"value":"radionuclide"},{"value":"radioecology"},{"value":"soil"},{"value":"sediment"},{"value":"water"},{"value":"uranium"},{"value":"thorium"},{"value":"potassium-40"}],"type":"discipline"},{"keywords":[{"value":"Human health and safety"},{"value":"Soil"},{"value":"radioecology"},{"value":"radioactive substance"}],"type":"theme"},{"keywords":[{"value":"STAR_NOE"}],"type":"discipline"}],"downloadOrder":{},"boundingBoxes":[{"westBoundLongitude":-6.67,"eastBoundLongitude":2.59,"southBoundLatitude":49.62,"northBoundLatitude":56.12}],"metadataPointsOfContact":[{"organisationName":"Centre for Ecology & Hydrology","role":"pointOfContact","email":"enquiries@ceh.ac.uk","address":{"deliveryPoint":"Lancaster Environment Centre,  Library Avenue, Bailrigg","city":"Lancaster","administrativeArea":"Lancashire","postalCode":"LA1 4AP","country":"United Kingdom"}}],"distributorContacts":[{"organisationName":"Centre for Ecology & Hydrology","role":"distributor","email":"enquiries@ceh.ac.uk","address":{"deliveryPoint":"Maclean Building, Benson Lane, Crowmarsh Gifford","city":"Wallingford","administrativeArea":"Oxfordshire ","postalCode":"OX10 8BB","country":"United Kingdom"}}],"responsibleParties":[{"individualName":"Beresford, N.A.","organisationName":"Centre for Ecology and Hydrology","role":"pointOfContact","email":"enquiries@ceh.ac.uk","address":{"deliveryPoint":"Lancaster Environment Centre,  Library Avenue, Bailrigg","city":"Lancaster","postalCode":"LA1 4AP","country":"UK"}},{"organisationName":"Shore Section","role":"resourceProvider","email":"enquiries@ceh.ac.uk"},{"organisationName":"Natural Environment Research Council","role":"owner","email":"enquiries@ceh.ac.uk","address":{"deliveryPoint":"Polaris House, North Star Avenue","city":"Swindon","postalCode":"SN2 1EU","country":"UK"}}],"onlineResources":[{"url":"http://lasigpublic.nerc-lancaster.ac.uk/ArcGIS/services/Biogeochemistry/NaturalRadionuclides/MapServer/WMSServer?request=getCapabilities&service=WMS","name":"Web Map Service (WMS)","description":"Link to the GetCapabilities request for this service","type":"WMS_GET_CAPABILITIES"}],"documentLinks":[{"title":"Natural radionuclide concentrations in soil, water and sediments in England and Wales","href":"http://rod.ceh.ac.uk:8080/id/bb2d7874-7bf4-44de-aa43-348bd684a2fe","associationType":"dataset"}],"spatialReferenceSystems":[{"code":"CRS:84","codeSpace":"urn:ogc:def:crs:EPSG","title":"WGS 84 longitude-latitude (CRS:84)"}],"datasetReferenceDate":{"creationDate":"2007-05-01"},"metadataDate":"2015-04-16T11:14:36","resourceMaintenance":[{"frequencyOfUpdate":"unknown"}],"service":{"type":"view","couplingType":"tight","versions":["1.3.0"],"coupledResources":[{"operationName":"GetMap","identifier":"CEH:EIDC:#1271758286281"},{"operationName":"GetMap","identifier":"https://gateway.ceh.ac.uk/soapServices/CSWStartup?Service=CSW&Request=GetRecordById&Version=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetname=full&id=bb2d7874-7bf4-44de-aa43-348bd684a2fe"}],"containsOperations":[{"operationName":"GetCapabilities","platform":"WebService","url":"http://lasigpublic.nerc-lancaster.ac.uk/ArcGIS/services/Biogeochemistry/NaturalRadionuclides/MapServer/WMSServer?"},{"operationName":"GetMap","platform":"WebService","url":"http://lasigpublic.nerc-lancaster.ac.uk/ArcGIS/services/Biogeochemistry/NaturalRadionuclides/MapServer/WMSServer?"},{"operationName":"GetFeatureInfo","platform":"WebService","url":"http://lasigpublic.nerc-lancaster.ac.uk/ArcGIS/services/Biogeochemistry/NaturalRadionuclides/MapServer/WMSServer?"}]},"locations":["-6.67 49.62 2.59 56.12"],"associatedResources":[{"title":"Natural radionuclide concentrations in soil, water and sediments in England and Wales","href":"http://rod.ceh.ac.uk:8080/id/bb2d7874-7bf4-44de-aa43-348bd684a2fe","associationType":"dataset"}],"mapViewerUrl":"/maps#layers/fb495d1b-80a3-416b-8dc7-c85ed22ed1e3","mapViewable":true,"type":"service"};
    </script>
  </div>
</@skeleton.master>
