import { afterEach, describe, expect, it } from 'vitest'
import $ from 'jquery'
import { FORMATS } from '../src/formats'
import { FORMAT_DATALIST_ID, ensureFormatDatalist, pickFormat } from '../src/utils/formatPicker'

describe('formatPicker', () => {
  afterEach(() => $(`#${FORMAT_DATALIST_ID}`).remove())

  it('adds one datalist with an option per listed format, however often it is called', () => {
    ensureFormatDatalist()
    ensureFormatDatalist()

    const lists = $(`datalist#${FORMAT_DATALIST_ID}`)
    expect(lists.length).toBe(1)
    expect(lists.find('option').map((i, o) => o.value).get()).toEqual(FORMATS.map(f => f.name))
  })

  it('gives the canonical name and type for a listed format', () => {
    expect(pickFormat(' .csv ')).toEqual({ name: 'Comma-separated values (CSV)', type: 'text/csv' })
  })

  it('keeps unlisted text, trimmed, with no type', () => {
    expect(pickFormat(' GeoPackage ')).toEqual({ name: 'GeoPackage', type: undefined })
  })
})
