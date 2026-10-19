import { describe, expect, it } from 'vitest'
import proj4 from 'proj4'
import { chooseCrs, crsLabel, getCapabilitiesUrl, getLegendUrl, getMapUrl, parseCapabilities, toBbox } from '../src/wmsRequests'
import { CAPABILITIES_XML, MAPSERVER_CRS } from './capabilitiesFixture'

const LAEA = '+proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs'

describe('wmsRequests', () => {
  describe('parseCapabilities', () => {
    it('lists only the leaf named layers, with their own title, bounds and inherited CRS', () => {
      expect(parseCapabilities(CAPABILITIES_XML)).toEqual([
        { name: 'LC.A', title: 'Woodland', bounds: [[50, -8], [60, 2]], crs: MAPSERVER_CRS },
        { name: 'LC.B', title: 'Conifer', bounds: null, crs: MAPSERVER_CRS }
      ])
    })

    it('skips unnamed groups and collects CRS from every ancestor', () => {
      const xml = `<WMS_Capabilities version="1.3.0" xmlns="http://www.opengis.net/wms"><Capability>
        <Layer><Title>Root</Title><CRS>EPSG:27700</CRS>
          <Layer><Title>Group</Title><CRS>EPSG:3035</CRS>
            <Layer><Name>leaf</Name><Title>Leaf</Title><CRS>EPSG:2157</CRS></Layer>
          </Layer>
        </Layer>
      </Capability></WMS_Capabilities>`
      const [leaf, ...rest] = parseCapabilities(xml)
      expect(rest).toEqual([])
      expect(leaf.name).toBe('leaf')
      expect(leaf.title).toBe('Leaf')
      expect([...leaf.crs].sort()).toEqual(['EPSG:2157', 'EPSG:27700', 'EPSG:3035'])
    })

    it('returns an empty list for a document without layers', () => {
      expect(parseCapabilities('<WMS_Capabilities/>')).toEqual([])
    })
  })

  describe('crsLabel', () => {
    it('names British National Grid, and gives other codes as they are', () => {
      expect(crsLabel('EPSG:27700')).toBe('British National Grid, EPSG:27700')
      expect(crsLabel('EPSG:3035')).toBe('EPSG:3035')
    })
  })

  describe('chooseCrs', () => {
    const gb = { south: 52, west: -3, north: 54, east: 0 }
    const dublin = { south: 53.2, west: -6.5, north: 53.5, east: -6 }
    // West of BNG's area of use (west -9.01). Dublin itself lies inside BNG's bounding box.
    const galway = { south: 53, west: -9.6, north: 53.5, east: -8.5 }
    const france = { south: 46, west: 1, north: 48, east: 4 }
    const world = { south: -85, west: -180, north: 85, east: 180 }

    it('prefers British National Grid for a view of Great Britain', () => {
      expect(chooseCrs(MAPSERVER_CRS, gb)).toBe('EPSG:27700')
    })

    it('uses Irish Transverse Mercator over Ireland when the layer has no BNG', () => {
      expect(chooseCrs(['EPSG:3035', 'EPSG:2157'], dublin)).toBe('EPSG:2157')
    })

    it('uses Irish Grid when that is the only Irish CRS offered', () => {
      expect(chooseCrs(['EPSG:29903', 'EPSG:3035'], dublin)).toBe('EPSG:29903')
    })

    it('uses ETRS89 LAEA when the view leaves BNG\'s area', () => {
      expect(chooseCrs(['EPSG:27700', 'EPSG:3035'], galway)).toBe('EPSG:3035')
    })

    it('uses ETRS89 LAEA for France from the standard MapServer list', () => {
      expect(chooseCrs(MAPSERVER_CRS, france)).toBe('EPSG:3035')
    })

    it('uses CRS:84 for the whole world', () => {
      expect(chooseCrs(MAPSERVER_CRS, world)).toBe('CRS:84')
    })

    it('clamps a view beyond the antimeridian before testing', () => {
      expect(chooseCrs(MAPSERVER_CRS, { south: -89, west: -400, north: 89, east: 400 })).toBe('CRS:84')
    })

    it('falls back to EPSG:4326, then EPSG:3857', () => {
      expect(chooseCrs(['EPSG:27700', 'EPSG:4326'], france)).toBe('EPSG:4326')
      expect(chooseCrs(['EPSG:3857', 'EPSG:27700'], france)).toBe('EPSG:3857')
    })

    it('returns null when the layer lists no supported CRS', () => {
      expect(chooseCrs(['EPSG:4258'], gb)).toBeNull()
      expect(chooseCrs(undefined, gb)).toBeNull()
    })
  })

  describe('toBbox', () => {
    it('projects Caister water tower to British National Grid', () => {
      // The OS worked example (651410, 313177) starts from OSGB36 latitude and longitude, which
      // is about 130 m away from the WGS84 view coordinates Leaflet supplies; this is the same
      // point expressed in WGS84, so a ten metre tolerance still checks the Helmert shift.
      const lat = 52.65757031 + 0.000345
      const lon = 1.7179216 - 0.0019
      const box = toBbox('EPSG:27700', { south: lat - 0.00001, north: lat + 0.00001, west: lon - 0.00001, east: lon + 0.00001 })
      expect(Math.abs(box.minX - 651410)).toBeLessThan(10)
      expect(Math.abs(box.maxX - 651410)).toBeLessThan(10)
      expect(Math.abs(box.minY - 313177)).toBeLessThan(10)
      expect(Math.abs(box.maxY - 313177)).toBeLessThan(10)
    })

    it('gives finite values for a whole-world view in every CRS', () => {
      ['EPSG:27700', 'EPSG:2157', 'EPSG:29903', 'EPSG:3035', 'CRS:84', 'EPSG:4326', 'EPSG:3857'].forEach(crs => {
        const box = toBbox(crs, { south: -90, west: -180, north: 90, east: 180 })
        Object.values(box).forEach(v => expect(Number.isFinite(v)).toBe(true))
        expect(box.minX).toBeLessThan(box.maxX)
        expect(box.minY).toBeLessThan(box.maxY)
      })
    })
  })

  describe('getMapUrl', () => {
    const view = { south: 52, west: -3, north: 54, east: 0 }
    const base = 'https://catalogue.ceh.ac.uk/maps/abc'
    const bboxOf = url => /BBOX=([^&]+)/.exec(url)[1].split(',')
    const crsOf = url => /CRS=([^&]+)/.exec(url)[1]

    it('builds an encoded GetMap URL', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'a b&c', crs: 'EPSG:27700', ...view, width: 800, height: 400 })
      expect(url.startsWith(base + '?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&LAYERS=a%20b%26c&STYLES=&CRS=EPSG:27700&BBOX=')).toBe(true)
      expect(url.endsWith('&WIDTH=800&HEIGHT=400&FORMAT=image/png&TRANSPARENT=TRUE')).toBe(true)
    })

    it('gives British National Grid easting first in whole metres', () => {
      const [minE, minN, maxE, maxN] = bboxOf(getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:27700', ...view, width: 400, height: 400 })).map(Number)
      ;[minE, minN, maxE, maxN].forEach(v => expect(Number.isInteger(v)).toBe(true))
      // Around 52-54N, 3W-0: eastings ~ 330-470 km, northings ~ 270-500 km
      expect(minE).toBeGreaterThan(250000)
      expect(maxE).toBeLessThan(550000)
      expect(minN).toBeGreaterThan(150000)
      expect(maxN).toBeLessThan(600000)
    })

    it('gives ETRS89 LAEA northing first', () => {
      const galway = { south: 53, west: -9.6, north: 53.5, east: -8.5 }
      const [x, y] = proj4('EPSG:4326', LAEA, [-9.05, 53.25])
      expect(Math.abs(x - y)).toBeGreaterThan(100000)

      const url = getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:3035', ...galway, width: 400, height: 300 })
      const [first, second, third, fourth] = bboxOf(url).map(Number)
      expect(crsOf(url)).toBe('EPSG:3035')
      expect(Math.abs((first + third) / 2 - y)).toBeLessThan(5000)
      expect(Math.abs((second + fourth) / 2 - x)).toBeLessThan(5000)
      ;[first, second, third, fourth].forEach(v => expect(Number.isInteger(v)).toBe(true))
    })

    it('gives CRS:84 longitude first in degrees to six decimals', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'l', crs: 'CRS:84', south: -85, west: -180, north: 85, east: 180, width: 800, height: 400 })
      const bbox = bboxOf(url)
      expect(crsOf(url)).toBe('CRS:84')
      bbox.forEach(v => expect(v).toMatch(/^-?\d+(\.\d{1,6})?$/))
      const [minLon, minLat, maxLon, maxLat] = bbox.map(Number)
      expect(minLon).toBe(-180)
      expect(maxLon).toBe(180)
      expect(minLat).toBeGreaterThanOrEqual(-90)
      expect(maxLat).toBeLessThanOrEqual(90)
    })

    it('rounds degrees outwards to six decimals', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'l', crs: 'CRS:84', south: 52.1234561, west: -3.1234561, north: 52.5234569, east: -2.7234569, width: 400, height: 400 })
      expect(bboxOf(url)).toEqual(['-3.123457', '52.123456', '-2.723456', '52.523457'])
    })

    it('gives EPSG:4326 latitude first', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:4326', ...view, width: 600, height: 400 })
      expect(bboxOf(url)).toEqual(['52', '-3', '54', '0'])
    })

    it('gives Web Mercator easting first in metres', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:3857', ...view, width: 400, height: 400 })
      const [minX, minY, maxX, maxY] = bboxOf(url).map(Number)
      // 3W-0 is about -334 km to 0 east; 52-54N is about 6.8-7.2 Mm north
      expect(minX).toBeGreaterThan(-400000)
      expect(minX).toBeLessThan(-300000)
      expect(maxX).toBeGreaterThan(0)
      expect(minY).toBeGreaterThan(6600000)
      expect(maxY).toBeLessThan(7400000)
    })

    it('matches the bbox aspect ratio to the pixel size', () => {
      [[800, 400], [300, 900]].forEach(([width, height]) => {
        const url = getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:27700', ...view, width, height })
        const [minE, minN, maxE, maxN] = bboxOf(url).map(Number)
        expect(Math.abs((maxE - minE) / (maxN - minN) / (width / height) - 1)).toBeLessThan(0.01)
      })
    })

    it('caps the size at 2048 keeping proportions', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:27700', ...view, width: 4096, height: 1024 })
      expect(url).toContain('&WIDTH=2048&HEIGHT=512&')
    })

    it('honours a custom cap', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:27700', ...view, width: 400, height: 200, maxSize: 100 })
      expect(url).toContain('&WIDTH=100&HEIGHT=50&')
    })

    it('returns an empty string when the map has no size', () => {
      expect(getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:27700', ...view, width: 0, height: 400 })).toBe('')
      expect(getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:27700', ...view, width: 400, height: 0 })).toBe('')
    })

    it('returns an empty string without a supported CRS', () => {
      expect(getMapUrl({ baseUrl: base, layer: 'l', crs: null, ...view, width: 400, height: 400 })).toBe('')
      expect(getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:4258', ...view, width: 400, height: 400 })).toBe('')
    })

    it('is finite for a whole-world view', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'l', crs: 'EPSG:27700', south: -85, west: -180, north: 85, east: 180, width: 800, height: 400 })
      expect(url).not.toMatch(/NaN|Infinity/)
    })
  })

  describe('getLegendUrl', () => {
    it('builds an encoded GetLegendGraphic URL', () => {
      expect(getLegendUrl('/maps/abc', 'a b')).toBe('/maps/abc?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetLegendGraphic&LAYER=a%20b&FORMAT=image/png&SLD_VERSION=1.1.0')
    })
  })

  describe('getCapabilitiesUrl', () => {
    it('builds a GetCapabilities URL', () => {
      expect(getCapabilitiesUrl('/maps/abc')).toBe('/maps/abc?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities')
    })
  })
})
