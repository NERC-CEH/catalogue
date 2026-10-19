// Shaped like MapServer's: a named root group (titled with the service) holding the data layers,
// with the CRS list declared once on the root and inherited by every layer.
export const MAPSERVER_CRS = ['CRS:84', 'EPSG:4326', 'EPSG:3857', 'EPSG:3035', 'EPSG:27700', 'EPSG:4258']

export const capabilitiesXml = ({ crs = MAPSERVER_CRS } = {}) => `<?xml version="1.0" encoding="UTF-8"?>
<WMS_Capabilities version="1.3.0" xmlns="http://www.opengis.net/wms" xmlns:xlink="http://www.w3.org/1999/xlink">
  <Service>
    <Name>WMS</Name>
    <Title>Test service</Title>
  </Service>
  <Capability>
    <Layer>
      <Name>WMS</Name>
      <Title>Test service</Title>
      ${crs.map(code => `<CRS>${code}</CRS>`).join('\n      ')}
      <Layer>
        <Name>LC.A</Name>
        <Title>Woodland</Title>
        <EX_GeographicBoundingBox>
          <westBoundLongitude>-8</westBoundLongitude>
          <eastBoundLongitude>2</eastBoundLongitude>
          <southBoundLatitude>50</southBoundLatitude>
          <northBoundLatitude>60</northBoundLatitude>
        </EX_GeographicBoundingBox>
      </Layer>
      <Layer>
        <Name>LC.B</Name>
        <Title>Conifer</Title>
      </Layer>
    </Layer>
  </Capability>
</WMS_Capabilities>`

export const CAPABILITIES_XML = capabilitiesXml()
