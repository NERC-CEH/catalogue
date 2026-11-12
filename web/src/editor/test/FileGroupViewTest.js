import { describe, expect, it } from 'vitest'
import Backbone from 'backbone'
import FileGroupView from '../src/views/service-agreement/FileGroupView'

const build = (attrs = {}) => {
  const model = new Backbone.Model(attrs)
  const view = new FileGroupView({ model, index: 'c7' })
  return { model, view }
}

const edit = (view, name, value) => {
  const input = view.$(`[data-name="${name}"]`)
  input.val(value).trigger('change')
}

describe('FileGroupView', () => {
  it('renders an input per field with ids built from the row index', () => {
    const { view } = build()

    for (const field of ['pattern', 'count', 'format', 'size', 'description']) {
      const input = view.$(`[data-name="${field}"]`)
      expect(input.length).toBe(1)
      expect(input.attr('id')).toBe(`fileGroupsc7${field[0].toUpperCase()}${field.slice(1)}`)
      expect(view.$(`label[for="${input.attr('id')}"]`).length).toBe(1)
    }
  })

  it('shows the values already in the model', () => {
    const { view } = build({ pattern: 'a/*.csv', count: 12, format: 'csv', size: '3Mb', description: 'Sites' })

    expect(view.$('[data-name="pattern"]').val()).toBe('a/*.csv')
    expect(view.$('[data-name="count"]').val()).toBe('12')
    expect(view.$('[data-name="description"]').val()).toBe('Sites')
  })

  it('updates the model when the inputs change', () => {
    const { model, view } = build()

    edit(view, 'pattern', 'CP_*_MInv_*Stats_*.csv')
    edit(view, 'format', 'csv')
    edit(view, 'size', '2-2.5 MB')
    edit(view, 'description', 'Chemistry')

    expect(model.get('pattern')).toBe('CP_*_MInv_*Stats_*.csv')
    expect(model.get('format')).toBe('csv')
    expect(model.get('size')).toBe('2-2.5 MB')
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
})
