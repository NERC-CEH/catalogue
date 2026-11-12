import { describe, expect, it } from 'vitest'
import Backbone from 'backbone'
import FileView from '../src/views/service-agreement/FileView'
import { predefinedFormats } from '../src/formats'
import { ServiceAgreementEditorView } from '../src/editors'
import { EditorMetadata } from '../src'

const NOT_RECOGNISED = 'Size not recognised, e.g. 32 MB or 2-2.5 MB'

const build = (attrs = {}) => {
  const model = new Backbone.Model(attrs)
  const view = new FileView({ model, index: 'c3' })
  return { model, view }
}

const edit = (view, name, value) => view.$(`[data-name="${name}"]`).val(value).trigger('change')
const sizeError = view => view.$('[data-name="sizeEntry"]').siblings('.size-error')

describe('FileView', () => {
  describe('format', () => {
    it('offers the shared format list', () => {
      const { view } = build()

      expect(view.$('[data-name="format"]').attr('list')).toBe('catalogue-formats')
      expect(document.getElementById('catalogue-formats')).not.toBeNull()
    })

    it('gives a listed format its canonical name and media type', () => {
      const { model, view } = build()

      edit(view, 'format', 'csv')

      expect(model.get('format')).toBe('Comma-separated values (CSV)')
      expect(model.get('formatType')).toBe('text/csv')
      expect(view.$('[data-name="format"]').val()).toBe('Comma-separated values (CSV)')
    })

    it('keeps an unlisted format as typed, with no media type', () => {
      const { model, view } = build({ format: 'Comma-separated values (CSV)', formatType: 'text/csv' })

      edit(view, 'format', 'GeoPackage')

      expect(model.get('format')).toBe('GeoPackage')
      expect(model.has('formatType')).toBe(false)
    })

    it('drops the format and its media type when cleared', () => {
      const { model, view } = build({ format: 'NetCDF', formatType: 'application/netcdf' })

      edit(view, 'format', '  ')

      expect(model.has('format')).toBe(false)
      expect(model.has('formatType')).toBe(false)
    })
  })

  describe('size', () => {
    it('keeps the label pointing at the size box', () => {
      const { view } = build()
      const input = view.$('[data-name="sizeEntry"]')

      expect(view.$(`label[for="${input.attr('id')}"]`).length).toBe(1)
      expect(sizeError(view).attr('role')).toBe('alert')
    })

    it('stores a size in bytes and shows it formatted', () => {
      const { model, view } = build()

      edit(view, 'sizeEntry', '32mb')

      expect(model.get('bytes')).toBe(33554432)
      expect(model.has('size')).toBe(false)
      expect(model.has('sizeEntry')).toBe(false)
      expect(view.$('[data-name="sizeEntry"]').val()).toBe('32 MB')
      expect(sizeError(view).text()).toBe('')
    })

    it('shows the stored bytes formatted', () => {
      const { view } = build({ bytes: 2364579 })

      expect(view.$('[data-name="sizeEntry"]').val()).toBe('2.26 MB')
    })

    for (const text of ['2-3 MB', 'nonsense']) {
      it(`leaves the model alone and says so for "${text}", until a readable size`, () => {
        const { model, view } = build({ bytes: 1024 })

        edit(view, 'sizeEntry', text)

        expect(model.attributes).toEqual({ bytes: 1024 })
        expect(sizeError(view).text()).toBe(NOT_RECOGNISED)
        expect(sizeError(view).is('[hidden]')).toBe(false)
        expect(view.$('[data-name="sizeEntry"]').val()).toBe(text)

        edit(view, 'sizeEntry', '3 KB')

        expect(model.get('bytes')).toBe(3072)
        expect(sizeError(view).text()).toBe('')
        expect(sizeError(view).is('[hidden]')).toBe(true)
      })
    }

    it('keeps the exact bytes when another field is edited', () => {
      const { model, view } = build({ bytes: 2364579 })

      edit(view, 'name', 'site1/2024.csv')
      edit(view, 'format', 'csv')

      expect(model.get('bytes')).toBe(2364579)
      expect(view.$('[data-name="sizeEntry"]').val()).toBe('2.26 MB')
    })

    it('drops the size when the box is cleared', () => {
      const { model, view } = build({ bytes: 1024, size: '1Kb' })

      edit(view, 'sizeEntry', '')

      expect(model.has('bytes')).toBe(false)
      expect(model.has('size')).toBe(false)
    })
  })

  describe('legacy size text', () => {
    it('is shown as typed when there are no bytes', () => {
      const { view } = build({ size: '32Mb' })

      expect(view.$('[data-name="sizeEntry"]').val()).toBe('32Mb')
    })

    it('is shown as text, not markup', () => {
      const { view } = build({ size: '<img src=x>' })

      expect(view.$('img').length).toBe(0)
      expect(view.$('[data-name="sizeEntry"]').val()).toBe('<img src=x>')
    })

    it('is replaced by bytes on a readable edit', () => {
      const { model, view } = build({ size: '32Mb' })

      edit(view, 'sizeEntry', '40 MB')

      expect(model.get('bytes')).toBe(41943040)
      expect(model.has('size')).toBe(false)
    })

    it('is kept on an unreadable edit', () => {
      const { model, view } = build({ size: '32Mb' })

      edit(view, 'sizeEntry', 'nonsense')

      expect(model.get('size')).toBe('32Mb')
      expect(model.has('bytes')).toBe(false)
      expect(sizeError(view).text()).toBe(NOT_RECOGNISED)
    })
  })

  it('still keeps the path as typed', () => {
    const { model, view } = build()

    edit(view, 'name', 'site1/2024.csv')

    expect(model.get('name')).toBe('site1/2024.csv')
  })
})

describe('the Files presets', () => {
  it('come from the shared list, by name and media type', () => {
    const editor = new ServiceAgreementEditorView({ model: new EditorMetadata({}) })
    const files = editor.sections
      .flatMap(section => section.views)
      .find(v => v.data && v.data.modelAttribute === 'files')

    expect(files.data.predefined).toEqual(predefinedFormats(e => ({ format: e.name, formatType: e.type })))
    expect(Object.keys(files.data.predefined)).toContain('Comma-separated values (CSV)')
  })
})
