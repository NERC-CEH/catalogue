import proj4 from 'proj4'

const WGS84 = 'EPSG:4326'
const WMS_PARAMS = 'SERVICE=WMS&VERSION=1.3.0'
const WORLD = [-180, -85.06, 180, 85.06]
// The island of Ireland. A view centred here never uses British National Grid, Northern Ireland included.
const IRELAND = [-10.56, 51.39, -5.34, 55.43]
// How far beyond its area of use a regional CRS still projects a view, so a wide view keeps its
// edges yet a near-world one stays finite
const PROJECTION_MARGIN = 30

/**
 * The CRSs a copyable GetMap can use. Each has its proj4 definition (null for plain longitude and
 * latitude), area of use as [west, south, east, north] in degrees, the areas (if any) where it is
 * never chosen though its area of use covers them, whether WMS 1.3.0 puts its
 * northing (or latitude) first in BBOX, and whether its units are degrees rather than metres.
 */
const CRS = {
  'EPSG:27700': {
    proj: '+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +towgs84=446.448,-125.157,542.06,0.15,0.247,0.842,-20.489 +units=m +no_defs',
    area: [-9.01, 49.75, 2.01, 61.01],
    excluded: [IRELAND]
  },
  'EPSG:2157': {
    proj: '+proj=tmerc +lat_0=53.5 +lon_0=-8 +k=0.99982 +x_0=600000 +y_0=750000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs',
    area: [-10.56, 51.39, -5.34, 55.43]
  },
  'EPSG:29903': {
    proj: '+proj=tmerc +lat_0=53.5 +lon_0=-8 +k=1.000035 +x_0=200000 +y_0=250000 +ellps=mod_airy +towgs84=482.5,-130.6,564.6,-1.042,-0.214,-0.631,8.15 +units=m +no_defs',
    area: [-10.56, 51.39, -5.34, 55.43]
  },
  'EPSG:3035': {
    proj: '+proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs',
    area: [-35.58, 24.60, 44.83, 84.73],
    northFirst: true
  },
  'CRS:84': { proj: null, area: [-180, -90, 180, 90], degrees: true, world: true },
  'EPSG:4326': { proj: null, area: [-180, -90, 180, 90], degrees: true, northFirst: true, world: true },
  'EPSG:3857': { proj: 'EPSG:3857', area: WORLD, world: true }
}
const PREFERRED = ['EPSG:27700', 'EPSG:2157', 'EPSG:29903', 'EPSG:3035', 'CRS:84']
const FALLBACK = ['CRS:84', 'EPSG:4326', 'EPSG:3857']
// Points along each edge of the view, as projected edges curve
const EDGE_STEPS = 8

const clamp = (value, min, max) => Math.min(Math.max(value, min), max)

const contains = ([west, south, east, north], [lon, lat]) => lon >= west && lon <= east && lat >= south && lat <= north

const clampTo = ([west, south, east, north], view) => ({
  west: clamp(view.west, west, east),
  east: clamp(view.east, west, east),
  south: clamp(view.south, south, north),
  north: clamp(view.north, south, north)
})

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

const isLayer = element => element?.localName === 'Layer'
const isNamed = layer => childText(layer, 'Name') !== null

// WMS 1.3.0: a layer offers its own CRSs and those of every ancestor layer
const crsOf = (layer) => {
  const codes = new Set()
  for (let el = layer; isLayer(el); el = el.parentElement) {
    Array.from(el.children).filter(c => c.localName === 'CRS').forEach(c => codes.add(c.textContent.trim()))
  }
  return [...codes]
}

/**
 * The leaf named layers of a WMS 1.3.0 capabilities document, in document order. A named group
 * holding named layers (such as MapServer's root layer) is left out.
 * @returns {{name: string, title: string, bounds: number[][]|null, crs: string[]}[]}
 */
export function parseCapabilities (xmlText) {
  const doc = new DOMParser().parseFromString(xmlText, 'application/xml')
  return Array.from(doc.getElementsByTagNameNS('*', 'Layer'))
    .filter(isNamed)
    .filter(layer => !Array.from(layer.getElementsByTagNameNS('*', 'Layer')).some(isNamed))
    .map(layer => ({
      name: childText(layer, 'Name'),
      title: childText(layer, 'Title'),
      bounds: boundsOf(layer),
      crs: crsOf(layer)
    }))
}

/** How the GetMap field names a CRS. */
export function crsLabel (crs) {
  return crs === 'EPSG:27700' ? 'British National Grid, EPSG:27700' : crs
}

/**
 * The CRS for a copyable GetMap of this view: the first preferred CRS the layer lists whose area of
 * use holds the view's centre (outside any area it is excluded from), else the first world CRS it
 * lists, else null. The centre, not the whole view, so a wide view of Great Britain stays on BNG.
 */
export function chooseCrs (layerCrs = [], view) {
  const { west, south, east, north } = clampTo(WORLD, view)
  const centre = [(west + east) / 2, (south + north) / 2]
  const eligible = ({ area, excluded = [] }) => contains(area, centre) && !excluded.some(box => contains(box, centre))
  return PREFERRED.find(crs => layerCrs.includes(crs) && eligible(CRS[crs])) ||
    FALLBACK.find(crs => layerCrs.includes(crs)) ||
    null
}

// Where a view is clamped before projecting: a world CRS's area of use, or a regional one's widened
// by PROJECTION_MARGIN degrees (within the world)
const limitsOf = ({ area: [west, south, east, north], world }) => world
  ? [west, south, east, north]
  : [
      Math.max(west - PROJECTION_MARGIN, WORLD[0]),
      Math.max(south - PROJECTION_MARGIN, WORLD[1]),
      Math.min(east + PROJECTION_MARGIN, WORLD[2]),
      Math.min(north + PROJECTION_MARGIN, WORLD[3])
    ]

/**
 * Bounding box of a WGS84 view in a CRS's own units, x easting (or longitude) and y northing
 * (or latitude). The view is clamped first (see limitsOf) so the projection stays finite.
 */
export function toBbox (crs, view) {
  const { proj } = CRS[crs]
  const { west, south, east, north } = clampTo(limitsOf(CRS[crs]), view)
  const lons = Array.from({ length: EDGE_STEPS + 1 }, (_, i) => west + (east - west) * i / EDGE_STEPS)
  const lats = Array.from({ length: EDGE_STEPS + 1 }, (_, i) => south + (north - south) * i / EDGE_STEPS)
  const edges = [
    ...lons.flatMap(lon => [[lon, south], [lon, north]]),
    ...lats.flatMap(lat => [[west, lat], [east, lat]])
  ]
  const points = proj ? edges.map(point => proj4(WGS84, proj, point)) : edges
  const xs = points.map(p => p[0])
  const ys = points.map(p => p[1])
  return { minX: Math.min(...xs), minY: Math.min(...ys), maxX: Math.max(...xs), maxY: Math.max(...ys) }
}

// Widens the narrower dimension about the centre so the bbox has the pixels' aspect ratio
const withAspect = ({ minX, minY, maxX, maxY }, aspect) => {
  const centreX = (minX + maxX) / 2
  const centreY = (minY + maxY) / 2
  let halfWidth = (maxX - minX) / 2
  let halfHeight = (maxY - minY) / 2
  if (halfWidth / halfHeight < aspect) halfWidth = halfHeight * aspect
  else halfHeight = halfWidth / aspect
  return { minX: centreX - halfWidth, minY: centreY - halfHeight, maxX: centreX + halfWidth, maxY: centreY + halfHeight }
}

// Rounds outwards: whole metres, or degrees to six decimals kept within valid longitudes and latitudes
const rounded = ({ minX, minY, maxX, maxY }, degrees) => {
  if (!degrees) return { minX: Math.floor(minX), minY: Math.floor(minY), maxX: Math.ceil(maxX), maxY: Math.ceil(maxY) }
  const down = (v, limit) => Math.max(Math.floor(v * 1e6) / 1e6, -limit)
  const up = (v, limit) => Math.min(Math.ceil(v * 1e6) / 1e6, limit)
  return { minX: down(minX, 180), minY: down(minY, 90), maxX: up(maxX, 180), maxY: up(maxY, 90) }
}

/**
 * A WMS 1.3.0 GetMap URL for the view in the given CRS (see chooseCrs), or '' when the CRS is not
 * supported or the map has no size.
 */
export function getMapUrl ({ baseUrl, layer, crs, south, west, north, east, width, height, maxSize = 2048 }) {
  if (!CRS[crs] || !(width > 0) || !(height > 0)) return ''
  const { northFirst, degrees } = CRS[crs]
  const { minX, minY, maxX, maxY } = rounded(withAspect(toBbox(crs, { south, west, north, east }), width / height), degrees)
  const bbox = northFirst ? [minY, minX, maxY, maxX] : [minX, minY, maxX, maxY]
  const scale = Math.min(1, maxSize / Math.max(width, height))
  const w = Math.round(width * scale)
  const h = Math.round(height * scale)
  return `${baseUrl}?${WMS_PARAMS}&REQUEST=GetMap&LAYERS=${encodeURIComponent(layer)}&STYLES=&CRS=${crs}` +
    `&BBOX=${bbox.join(',')}&WIDTH=${w}&HEIGHT=${h}&FORMAT=image/png&TRANSPARENT=TRUE`
}

export function getLegendUrl (localPath, layer) {
  return `${localPath}?${WMS_PARAMS}&REQUEST=GetLegendGraphic&LAYER=${encodeURIComponent(layer)}&FORMAT=image/png&SLD_VERSION=1.1.0`
}

export function getCapabilitiesUrl (localPath) {
  return `${localPath}?${WMS_PARAMS}&REQUEST=GetCapabilities`
}
