export const CAPABILITIES_XML = `<?xml version="1.0" encoding="UTF-8"?>
<WMS_Capabilities version="1.3.0" xmlns="http://www.opengis.net/wms" xmlns:xlink="http://www.w3.org/1999/xlink">
  <Service>
    <Name>WMS</Name>
    <Title>Test service</Title>
  </Service>
  <Capability>
    <Layer>
      <Title>Root</Title>
      <CRS>EPSG:27700</CRS>
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
