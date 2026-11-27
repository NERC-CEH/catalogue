import { describe, expect, it } from 'vitest'
import { TextareaView } from '../src/views'
import { ServiceAgreementEditorView } from '../src/editors'
import { EditorMetadata } from '../src'

const build = (attrs = {}) => {
  const model = new EditorMetadata(attrs)
  const editor = new ServiceAgreementEditorView({ model })
  const views = editor.sections.flatMap(section => section.views)
  const at = attribute => views.findIndex(v => v.data && v.data.modelAttribute === attribute)
  return { model, views, at }
}

describe('the service agreement naming convention', () => {
  it('is an editable text box after the file groups', () => {
    const { views, at } = build()
    const convention = at('fileNamingConvention')

    expect(views[convention]).toBeInstanceOf(TextareaView)
    expect(convention).toBeGreaterThan(at('fileGroups'))
  })

  it('shows an existing convention in the box', () => {
    const { views, at } = build({ fileNamingConvention: 'CP_<REGION>.csv' })

    expect(views[at('fileNamingConvention')].$('textarea').val()).toBe('CP_<REGION>.csv')
  })

  it('saves what is typed', () => {
    const { model, views, at } = build()

    views[at('fileNamingConvention')].$('textarea').val('200 files x_<year>.csv').trigger('change')

    expect(model.get('fileNamingConvention')).toBe('200 files x_<year>.csv')
  })

  it('frames files and groups as checked and the convention as a description', () => {
    const { views, at } = build()
    const files = at('files')
    const groups = at('fileGroups')
    const convention = at('fileNamingConvention')
    const checked = [files - 1, files, groups - 1, groups].map(i => views[i])
    const described = [convention - 1, convention].map(i => views[i])

    checked.forEach(v => expect(v.$el.hasClass('component--files-checked')).toBe(true))
    described.forEach(v => expect(v.$el.hasClass('component--files-described')).toBe(true))
    expect(views[files - 1].$el.text()).toContain('Checked against uploads')
    expect(views[groups - 1].$el.text()).toContain('Checked against uploads')
    expect(views[convention - 1].$el.text()).toContain('Description only – not checked against uploads')
  })

  it('labels the box Naming convention', () => {
    const { views, at } = build()
    const help = views[at('fileNamingConvention') - 1]

    expect(help.$el.text()).toContain('Naming convention')
  })
})
