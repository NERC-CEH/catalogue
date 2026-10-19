import { describe, expect, it } from 'vitest'
import { getCapabilitiesUrl, getLegendUrl, getMapUrl, parseCapabilities, toBng } from '../src/wmsRequests'
import { CAPABILITIES_XML } from './capabilitiesFixture'

describe('wmsRequests', () => {
  describe('parseCapabilities', () => {
    it('lists only named layers with their own title and bounds', () => {
      expect(parseCapabilities(CAPABILITIES_XML)).toEqual([
        { name: 'LC.A', title: 'Woodland', bounds: [[50, -8], [60, 2]] },
        { name: 'LC.B', title: 'Conifer', bounds: null }
      ])
    })

    it('returns an empty list for a document without layers', () => {
      expect(parseCapabilities('<WMS_Capabilities/>')).toEqual([])
    })
  })

  describe('toBng', () => {
    it('projects Caister water tower to British National Grid', () => {
      // The OS worked example (651410, 313177) starts from OSGB36 latitude and longitude, which
      // is about 130 m away from the WGS84 view coordinates Leaflet supplies; this is the same
      // point expressed in WGS84, so a ten metre tolerance still checks the Helmert shift.
      const lat = 52.65757031 + 0.000345
      const lon = 1.7179216 - 0.0019
      const bng = toBng({ south: lat - 0.00001, north: lat + 0.00001, west: lon - 0.00001, east: lon + 0.00001 })
      expect(Math.abs(bng.minE - 651410)).toBeLessThan(10)
      expect(Math.abs(bng.maxE - 651410)).toBeLessThan(10)
      expect(Math.abs(bng.minN - 313177)).toBeLessThan(10)
      expect(Math.abs(bng.maxN - 313177)).toBeLessThan(10)
    })

    it('gives finite whole-metre values for a whole-world view', () => {
      const bng = toBng({ south: -85, west: -180, north: 85, east: 180 })
      Object.values(bng).forEach(v => {
        expect(Number.isFinite(v)).toBe(true)
        expect(Number.isInteger(v)).toBe(true)
      })
      expect(bng.minE).toBeLessThan(bng.maxE)
      expect(bng.minN).toBeLessThan(bng.maxN)
    })
  })

  describe('getMapUrl', () => {
    const view = { south: 52, west: -3, north: 54, east: 0 }
    const base = 'https://catalogue.ceh.ac.uk/maps/abc'

    it('builds an encoded GetMap URL', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'a b&c', ...view, width: 800, height: 400 })
      expect(url.startsWith(base + '?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&LAYERS=a%20b%26c&STYLES=&CRS=EPSG:27700&BBOX=')).toBe(true)
      expect(url.endsWith('&WIDTH=800&HEIGHT=400&FORMAT=image/png&TRANSPARENT=TRUE')).toBe(true)
    })

    it('matches the bbox aspect ratio to the pixel size', () => {
      [[800, 400], [300, 900]].forEach(([width, height]) => {
        const url = getMapUrl({ baseUrl: base, layer: 'l', ...view, width, height })
        const [minE, minN, maxE, maxN] = /BBOX=([^&]+)/.exec(url)[1].split(',').map(Number)
        expect(Math.abs((maxE - minE) / (maxN - minN) / (width / height) - 1)).toBeLessThan(0.01)
      })
    })

    it('caps the size at 2048 keeping proportions', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'l', ...view, width: 4096, height: 1024 })
      expect(url).toContain('&WIDTH=2048&HEIGHT=512&')
    })

    it('honours a custom cap', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'l', ...view, width: 400, height: 200, maxSize: 100 })
      expect(url).toContain('&WIDTH=100&HEIGHT=50&')
    })

    it('returns an empty string when the map has no size', () => {
      expect(getMapUrl({ baseUrl: base, layer: 'l', ...view, width: 0, height: 400 })).toBe('')
      expect(getMapUrl({ baseUrl: base, layer: 'l', ...view, width: 400, height: 0 })).toBe('')
    })

    it('is finite for a whole-world view', () => {
      const url = getMapUrl({ baseUrl: base, layer: 'l', south: -85, west: -180, north: 85, east: 180, width: 800, height: 400 })
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
