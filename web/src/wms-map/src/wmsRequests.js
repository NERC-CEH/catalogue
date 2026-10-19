import proj4 from 'proj4'

const BNG = '+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +towgs84=446.448,-125.157,542.06,0.15,0.247,0.842,-20.489 +units=m +no_defs'
const WGS84 = 'EPSG:4326'
const WMS_PARAMS = 'SERVICE=WMS&VERSION=1.3.0'

const clamp = (value, min, max) => Math.min(Math.max(value, min), max)

const childText = (element, tagName) => {
  const child = Array.from(element.children).find(c => c.localName === tagName)
  return child ? child.textContent.trim() : null
}

const boundsOf = (layer) => {
  const box = Array.from(layer.children).find(c => c.localName === 'EX_GeographicBoundingBox')
  if (!box) return null
  const [west, east, south, north] = ['westBoundLongitude', 'eastBoundLongitude', 'southBoundLatitude', 'northBoundLatitude']
    .map(tag => Number(childText(box, tag)))
  return [west, east, south, north].every(Number.isFinite) ? [[south, west], [north, east]] : null
}

/**
 * Named layers of a WMS 1.3.0 capabilities document, in document order.
 * @returns {{name: string, title: string, bounds: number[][]|null}[]}
 */
export function parseCapabilities (xmlText) {
  const doc = new DOMParser().parseFromString(xmlText, 'application/xml')
  return Array.from(doc.getElementsByTagNameNS('*', 'Layer'))
    .filter(layer => childText(layer, 'Name') !== null)
    .map(layer => ({
      name: childText(layer, 'Name'),
      title: childText(layer, 'Title'),
      bounds: boundsOf(layer)
    }))
}

/**
 * British National Grid bounding box (whole metres) of a WGS84 view, clamped to the area around Great Britain.
 */
export function toBng ({ south, west, north, east }) {
  const lats = [south, north].map(v => clamp(v, 48, 62))
  const lons = [west, east].map(v => clamp(v, -10, 4))
  const points = lons.flatMap(lon => lats.map(lat => proj4(WGS84, BNG, [lon, lat])))
  const eastings = points.map(p => p[0])
  const northings = points.map(p => p[1])
  return {
    minE: Math.floor(Math.min(...eastings)),
    minN: Math.floor(Math.min(...northings)),
    maxE: Math.ceil(Math.max(...eastings)),
    maxN: Math.ceil(Math.max(...northings))
  }
}

// Widens the narrower dimension about the centre so the bbox has the pixels' aspect ratio.
const withAspect = ({ minE, minN, maxE, maxN }, aspect) => {
  const centreE = (minE + maxE) / 2
  const centreN = (minN + maxN) / 2
  let halfWidth = (maxE - minE) / 2
  let halfHeight = (maxN - minN) / 2
  if (halfWidth / halfHeight < aspect) halfWidth = halfHeight * aspect
  else halfHeight = halfWidth / aspect
  return {
    minE: Math.floor(centreE - halfWidth),
    minN: Math.floor(centreN - halfHeight),
    maxE: Math.ceil(centreE + halfWidth),
    maxN: Math.ceil(centreN + halfHeight)
  }
}

export function getMapUrl ({ baseUrl, layer, south, west, north, east, width, height, maxSize = 2048 }) {
  if (!(width > 0) || !(height > 0)) return ''
  const { minE, minN, maxE, maxN } = withAspect(toBng({ south, west, north, east }), width / height)
  const scale = Math.min(1, maxSize / Math.max(width, height))
  const w = Math.round(width * scale)
  const h = Math.round(height * scale)
  return `${baseUrl}?${WMS_PARAMS}&REQUEST=GetMap&LAYERS=${encodeURIComponent(layer)}&STYLES=&CRS=EPSG:27700` +
    `&BBOX=${minE},${minN},${maxE},${maxN}&WIDTH=${w}&HEIGHT=${h}&FORMAT=image/png&TRANSPARENT=TRUE`
}

export function getLegendUrl (localPath, layer) {
  return `${localPath}?${WMS_PARAMS}&REQUEST=GetLegendGraphic&LAYER=${encodeURIComponent(layer)}&FORMAT=image/png&SLD_VERSION=1.1.0`
}

export function getCapabilitiesUrl (localPath) {
  return `${localPath}?${WMS_PARAMS}&REQUEST=GetCapabilities`
}
