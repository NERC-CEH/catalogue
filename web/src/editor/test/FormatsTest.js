import { describe, expect, it } from 'vitest'
import FORMATS, { FORMATS as NAMED, findFormat, predefinedFormats } from '../src/formats'

const CSV = { name: 'Comma-separated values (CSV)', type: 'text/csv', extensions: ['csv'] }

describe('The format list', () => {
  it('is the default and a named export', () => {
    expect(FORMATS).toBe(NAMED)
  })

  it('holds the GEMINI editor seed entries', () => {
    expect(FORMATS).toEqual(expect.arrayContaining([
      CSV,
      { name: 'NetCDF', type: 'application/netcdf', extensions: ['nc'] },
      { name: 'Parquet', type: 'application/vnd.apache.parquet', extensions: ['parquet'] },
      { name: 'Shapefile', type: '', extensions: ['shp'] },
      { name: 'TIFF', type: 'image/tiff', extensions: ['tif', 'tiff'] }
    ]))
  })

  it('has unique names and extensions, ignoring case', () => {
    const names = FORMATS.map(f => f.name.toLowerCase())
    const extensions = FORMATS.flatMap(f => f.extensions.map(e => e.toLowerCase()))
    expect(new Set(names).size).toBe(names.length)
    expect(new Set(extensions).size).toBe(extensions.length)
  })

  it('writes extensions without a leading dot', () => {
    FORMATS.flatMap(f => f.extensions).forEach(e => expect(e).toMatch(/^[^.\s]+$/))
  })
})

describe('findFormat', () => {
  it.each(['csv', ' .CSV ', 'Comma-separated values (CSV)', 'comma-separated values (csv)'])(
    'finds CSV for %j', (text) => {
      expect(findFormat(text)).toEqual(CSV)
    })

  it.each(['GeoPackage', '', undefined, null, 'c sv'])('finds nothing for %j', (text) => {
    expect(findFormat(text)).toBeUndefined()
  })
})

describe('predefinedFormats', () => {
  it('has a key per entry, in list order, built by the callback', () => {
    const result = predefinedFormats(e => ({ format: e.name, formatType: e.type }))
    expect(Object.keys(result)).toEqual(FORMATS.map(f => f.name))
    expect(result['Comma-separated values (CSV)']).toEqual({ format: 'Comma-separated values (CSV)', formatType: 'text/csv' })
  })
})
