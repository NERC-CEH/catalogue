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

  describe('trying a file name against the pattern', () => {
    const tryName = (view, value) => view.$('.pattern-tester').val(value).trigger('input')
    const result = view => view.$('.pattern-tester-result').text()

    it('has a labelled box that is not part of the agreement', () => {
      const { model, view } = build({ pattern: '*.csv' })
      const input = view.$('.pattern-tester')

      expect(input.attr('id')).toBe('fileGroupsc7Try')
      expect(input.attr('data-name')).toBeUndefined()
      expect(view.$('label[for="fileGroupsc7Try"]').text()).toBe('Try a file name')
      expect(view.$('.pattern-tester-result').attr('aria-live')).toBe('polite')

      input.val('a.csv').trigger('input').trigger('change')
      expect(Object.keys(model.attributes)).toEqual(['pattern'])
    })

    it('says whether the name matches as it is typed', () => {
      const { view } = build({ pattern: 'CP_*_MInv_*Stats_*.csv' })

      expect(result(view)).toBe('')
      tryName(view, 'CP_north_MInv_nitrateStats_2020.csv')
      expect(result(view)).toBe('matches this pattern')
      tryName(view, 'CP_north_Stats.csv')
      expect(result(view)).toBe("doesn't match this pattern")
      tryName(view, '')
      expect(result(view)).toBe('')
    })

    it('compares as the upload page does, ignoring case and treating spaces as hyphens', () => {
      const { view } = build({ pattern: 'Site 1/*.csv' })

      tryName(view, 'site-1/Readings 2024.CSV')
      expect(result(view)).toBe('matches this pattern')
    })

    it('checks again when the pattern is edited', () => {
      const { view } = build({ pattern: '*.txt' })
      tryName(view, 'a.csv')
      expect(result(view)).toBe("doesn't match this pattern")

      view.$('[data-name="pattern"]').val('*.csv').trigger('input')
      expect(result(view)).toBe('matches this pattern')
    })

    it('says nothing until there is a pattern', () => {
      const { view } = build()

      tryName(view, 'a.csv')
      expect(result(view)).toBe('')
    })
  })
})
