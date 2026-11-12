import { describe, expect, it } from 'vitest'
import Backbone from 'backbone'
import FileGroupView from '../src/views/service-agreement/FileGroupView'

const NOT_RECOGNISED = 'Size not recognised, e.g. 32 MB or 2-2.5 MB'

const build = (attrs = {}) => {
  const model = new Backbone.Model(attrs)
  const view = new FileGroupView({ model, index: 'c7' })
  return { model, view }
}

const edit = (view, name, value) => {
  const input = view.$(`[data-name="${name}"]`)
  input.val(value).trigger('change')
}

const sizeError = (view, name) => view.$(`[data-name="${name}"]`).siblings('.size-error')

describe('FileGroupView', () => {
  it('renders an input per field with ids built from the row index', () => {
    const { view } = build()

    for (const field of ['pattern', 'count', 'format', 'eachSize', 'totalSize', 'description']) {
      const input = view.$(`[data-name="${field}"]`)
      expect(input.length).toBe(1)
      expect(input.attr('id')).toBe(`fileGroupsc7${field[0].toUpperCase()}${field.slice(1)}`)
      expect(view.$(`label[for="${input.attr('id')}"]`).length).toBe(1)
    }
  })

  it('shows the values already in the model', () => {
    const { view } = build({ pattern: 'a/*.csv', count: 12, format: 'csv', description: 'Sites' })

    expect(view.$('[data-name="pattern"]').val()).toBe('a/*.csv')
    expect(view.$('[data-name="count"]').val()).toBe('12')
    expect(view.$('[data-name="format"]').val()).toBe('csv')
    expect(view.$('[data-name="description"]').val()).toBe('Sites')
  })

  it('updates the model when the inputs change', () => {
    const { model, view } = build()

    edit(view, 'pattern', 'CP_*_MInv_*Stats_*.csv')
    edit(view, 'description', 'Chemistry')

    expect(model.get('pattern')).toBe('CP_*_MInv_*Stats_*.csv')
    expect(model.get('description')).toBe('Chemistry')
  })

  it('stores the count as a number and drops it when cleared', () => {
    const { model, view } = build()

    edit(view, 'count', '200')
    expect(model.get('count')).toBe(200)

    edit(view, 'count', '')
    expect(model.has('count')).toBe(false)
  })

  it('renders markup in a pattern as text', () => {
    const { view } = build({ pattern: '<img src=x>' })

    expect(view.$('img').length).toBe(0)
    expect(view.$('[data-name="pattern"]').val()).toBe('<img src=x>')
  })

  describe('format', () => {
    it('offers the shared format list', () => {
      const { view } = build()

      expect(view.$('[data-name="format"]').attr('list')).toBe('catalogue-formats')
    })

    it('gives a listed format its canonical name and media type', () => {
      const { model, view } = build()

      edit(view, 'format', 'csv')

      expect(model.get('format')).toBe('Comma-separated values (CSV)')
      expect(model.get('formatType')).toBe('text/csv')
      expect(view.$('[data-name="format"]').val()).toBe('Comma-separated values (CSV)')
    })

    it('keeps an unlisted format as typed, with no media type', () => {
      const { model, view } = build({ format: 'NetCDF', formatType: 'application/netcdf' })

      edit(view, 'format', 'GeoPackage')

      expect(model.get('format')).toBe('GeoPackage')
      expect(model.has('formatType')).toBe(false)
    })
  })

  describe('size of each file', () => {
    it('stores a range as the smallest and largest bytes, and shows it formatted', () => {
      const { model, view } = build()

      edit(view, 'eachSize', '2-2.5 MB')

      expect(model.get('minBytes')).toBe(2097152)
      expect(model.get('maxBytes')).toBe(2621440)
      expect(model.has('eachSize')).toBe(false)
      expect(view.$('[data-name="eachSize"]').val()).toBe('2 MB - 2.5 MB')
    })

    it('stores an exact size as both ends', () => {
      const { model, view } = build()

      edit(view, 'eachSize', '2 MB')

      expect(model.get('minBytes')).toBe(2097152)
      expect(model.get('maxBytes')).toBe(2097152)
      expect(view.$('[data-name="eachSize"]').val()).toBe('2 MB')
    })

    it('shows the stored bytes formatted', () => {
      expect(build({ minBytes: 2097152, maxBytes: 2621440 }).view.$('[data-name="eachSize"]').val()).toBe('2 MB - 2.5 MB')
      expect(build({ minBytes: 2364579, maxBytes: 2364579 }).view.$('[data-name="eachSize"]').val()).toBe('2.26 MB')
    })

    it('shows one end on its own, as an agreement saved through the API may have only one', () => {
      expect(build({ maxBytes: 2048, size: 'old' }).view.$('[data-name="eachSize"]').val()).toBe('2 KB')
      expect(build({ minBytes: 1024 }).view.$('[data-name="eachSize"]').val()).toBe('1 KB')
    })

    it('leaves the model alone and says so for unreadable text, until a readable size', () => {
      const { model, view } = build({ minBytes: 1024, maxBytes: 2048 })

      edit(view, 'eachSize', '>= 2 MB')

      expect(model.attributes).toEqual({ minBytes: 1024, maxBytes: 2048 })
      expect(sizeError(view, 'eachSize').text()).toBe(NOT_RECOGNISED)
      expect(sizeError(view, 'eachSize').attr('role')).toBe('alert')
      expect(sizeError(view, 'totalSize').text()).toBe('')

      edit(view, 'eachSize', '3 KB')

      expect(model.get('minBytes')).toBe(3072)
      expect(model.get('maxBytes')).toBe(3072)
      expect(sizeError(view, 'eachSize').text()).toBe('')
    })

    it('keeps the exact bytes when another field is edited', () => {
      const { model, view } = build({ minBytes: 2364579, maxBytes: 2364579, totalBytes: 472915800 })

      edit(view, 'pattern', 'a/*.csv')
      edit(view, 'count', '200')

      expect(model.get('minBytes')).toBe(2364579)
      expect(model.get('maxBytes')).toBe(2364579)
      expect(model.get('totalBytes')).toBe(472915800)
    })

    it('drops both ends when the box is cleared', () => {
      const { model, view } = build({ minBytes: 1024, maxBytes: 2048 })

      edit(view, 'eachSize', '')

      expect(model.has('minBytes')).toBe(false)
      expect(model.has('maxBytes')).toBe(false)
    })
  })

  describe('total size', () => {
    it('stores the total in bytes and shows it formatted', () => {
      const { model, view } = build()

      edit(view, 'totalSize', '443 MB')

      expect(model.get('totalBytes')).toBe(464519168)
      expect(model.has('totalSize')).toBe(false)
      expect(view.$('[data-name="totalSize"]').val()).toBe('443 MB')
    })

    it('shows the stored total formatted', () => {
      expect(build({ totalBytes: 464519168 }).view.$('[data-name="totalSize"]').val()).toBe('443 MB')
    })

    for (const text of ['2-3 MB', 'nonsense']) {
      it(`leaves the model alone and says so for "${text}"`, () => {
        const { model, view } = build({ totalBytes: 1024 })

        edit(view, 'totalSize', text)

        expect(model.attributes).toEqual({ totalBytes: 1024 })
        expect(sizeError(view, 'totalSize').text()).toBe(NOT_RECOGNISED)
        expect(sizeError(view, 'eachSize').text()).toBe('')

        edit(view, 'totalSize', '1 GB')

        expect(model.get('totalBytes')).toBe(1073741824)
        expect(sizeError(view, 'totalSize').text()).toBe('')
      })
    }

    it('drops the total when the box is cleared', () => {
      const { model, view } = build({ totalBytes: 1024 })

      edit(view, 'totalSize', '')

      expect(model.has('totalBytes')).toBe(false)
    })
  })

  describe('legacy size text', () => {
    it('is shown as typed in the size of each file when there are no per-file bytes', () => {
      const { view } = build({ size: '2-2.5 MB each, 443 MB in total' })

      expect(view.$('[data-name="eachSize"]').val()).toBe('2-2.5 MB each, 443 MB in total')
      expect(view.$('[data-name="totalSize"]').val()).toBe('')
    })

    it('is shown as text, not markup', () => {
      const { view } = build({ size: '<img src=x>' })

      expect(view.$('img').length).toBe(0)
    })

    it('is replaced by bytes on a readable edit', () => {
      const { model, view } = build({ size: '2-2.5 MB each' })

      edit(view, 'eachSize', '2-2.5 MB')

      expect(model.get('minBytes')).toBe(2097152)
      expect(model.get('maxBytes')).toBe(2621440)
      expect(model.has('size')).toBe(false)
    })

    it('is kept on an unreadable edit', () => {
      const { model, view } = build({ size: '2-2.5 MB each' })

      edit(view, 'eachSize', 'nonsense')

      expect(model.get('size')).toBe('2-2.5 MB each')
      expect(model.has('minBytes')).toBe(false)
      expect(sizeError(view, 'eachSize').text()).toBe(NOT_RECOGNISED)
    })

    it('is kept when only the total is given, as it stays in the size of each file', () => {
      const { model, view } = build({ size: '2-2.5 MB each' })

      edit(view, 'totalSize', '443 MB')

      expect(model.get('totalBytes')).toBe(464519168)
      expect(model.get('size')).toBe('2-2.5 MB each')
    })
  })
})
