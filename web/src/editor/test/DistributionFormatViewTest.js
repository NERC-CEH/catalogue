import { describe, expect, it } from 'vitest'
import $ from 'jquery'
import Backbone from 'backbone'
import { predefinedFormats } from '../src/formats'
import { GeminiEditorView, UkemsDocumentEditorView } from '../src/editors'
import { EditorMetadata } from '../src'
import { DistributionFormatView } from '../src/views'

const build = (attrs = {}) => {
  const model = new Backbone.Model(attrs)
  const view = new DistributionFormatView({ model, index: 0 })
  return { model, view }
}

const edit = (view, name, value) => view.$(`[data-name="${name}"]`).val(value).trigger('change')

describe('DistributionFormatView', () => {
  it('offers the shared format list on the name input', () => {
    const { view } = build()

    expect(view.$('[data-name="name"]').attr('list')).toBe('catalogue-formats')
    expect($('datalist#catalogue-formats option').length).toBeGreaterThan(0)
  })

  it('sets the canonical name and media type for a listed format', () => {
    const { model, view } = build({ version: '4' })

    edit(view, 'name', 'csv')

    expect(model.get('name')).toBe('Comma-separated values (CSV)')
    expect(model.get('type')).toBe('text/csv')
    expect(view.$('[data-name="name"]').val()).toBe('Comma-separated values (CSV)')
    expect(model.get('version')).toBe('4')
  })

  it('keeps an unlisted name and unsets the media type', () => {
    const { model, view } = build({ name: 'CSV', type: 'text/csv', version: '1' })

    edit(view, 'name', 'GeoPackage')

    expect(model.get('name')).toBe('GeoPackage')
    expect(model.has('type')).toBe(false)
    expect(model.get('version')).toBe('1')
  })

  it('still keeps the version as typed', () => {
    const { model, view } = build()

    edit(view, 'version', ' 2 ')

    expect(model.get('version')).toBe('2')
  })

  it('clears the name when emptied', () => {
    const { model, view } = build({ name: 'NetCDF', type: 'application/netcdf' })

    edit(view, 'name', '')

    expect(model.has('name')).toBe(false)
    expect(model.has('type')).toBe(false)
  })
})

describe('the Formats presets', () => {
  const expected = predefinedFormats(e => ({ name: e.name, type: e.type, version: 'unknown' }))
  const presetsOf = (editor) => editor.sections
    .flatMap(section => section.views)
    .find(v => v.data && v.data.modelAttribute === 'distributionFormats').data.predefined

  it('come from the shared list in the dataset editor', () => {
    const editor = new GeminiEditorView({ model: new EditorMetadata({ type: 'dataset' }) })
    expect(presetsOf(editor)).toEqual(expected)
  })

  it('come from the shared list in the UKEMS editor', () => {
    const editor = new UkemsDocumentEditorView({ model: new EditorMetadata({ type: 'dataset' }) })
    expect(presetsOf(editor)).toEqual(expected)
  })
})
