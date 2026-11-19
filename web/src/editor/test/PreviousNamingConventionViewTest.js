import { describe, expect, it } from 'vitest'
import Backbone from 'backbone'
import PreviousNamingConventionView from '../src/views/service-agreement/PreviousNamingConventionView'
import { ServiceAgreementEditorView } from '../src/editors'
import { EditorMetadata } from '../src'

const build = (attrs = {}) => {
  const model = new Backbone.Model(attrs)
  const view = new PreviousNamingConventionView({ model, modelAttribute: 'fileNamingConvention' })
  return { model, view }
}

describe('PreviousNamingConventionView', () => {
  it('shows nothing for an agreement without a naming convention', () => {
    expect(build().view.$el.children().length).toBe(0)
    expect(build({ fileNamingConvention: '  ' }).view.$el.children().length).toBe(0)
  })

  it('shows the convention read-only, labelled, with its line breaks', () => {
    const { view } = build({ fileNamingConvention: 'CP_<REGION>.csv\n200 files' })

    expect(view.$('label').text()).toContain('Previous naming convention')
    const text = view.$('.previous-naming-convention')
    expect(text.text()).toBe('CP_<REGION>.csv200 files')
    expect(text.find('br').length).toBe(1)
    expect(view.$('input, textarea').length).toBe(0)
  })

  it('shows a convention loaded after the view was built', () => {
    const { model, view } = build()

    model.set('fileNamingConvention', 'CP_<REGION>.csv')

    expect(view.$('.previous-naming-convention').text()).toBe('CP_<REGION>.csv')
  })

  it('escapes the text', () => {
    const { view } = build({ fileNamingConvention: '<img src=x onerror=alert(1)>' })

    expect(view.$('img').length).toBe(0)
    expect(view.$('.previous-naming-convention').text()).toBe('<img src=x onerror=alert(1)>')
  })

  it('asks for the files to be described as file groups', () => {
    const { view } = build({ fileNamingConvention: 'CP_<REGION>.csv' })

    expect(view.$el.text()).toMatch(/describe these files as file groups/i)
  })

  it('removes the convention from the agreement and then shows nothing', () => {
    const { model, view } = build({ fileNamingConvention: 'CP_<REGION>.csv' })

    view.$('button.remove-naming-convention').trigger('click')

    expect(model.has('fileNamingConvention')).toBe(false)
    expect(view.$el.children().length).toBe(0)
  })
})

describe('the service agreement editor', () => {
  it('shows a previous naming convention just before the file groups', () => {
    const editor = new ServiceAgreementEditorView({ model: new EditorMetadata({ fileNamingConvention: 'CP_<REGION>.csv' }) })
    const views = editor.sections.flatMap(section => section.views)
    const convention = views.findIndex(v => v instanceof PreviousNamingConventionView)
    const groups = views.findIndex(v => v.data && v.data.modelAttribute === 'fileGroups')

    expect(convention).toBeGreaterThan(-1)
    expect(views[convention].data.modelAttribute).toBe('fileNamingConvention')
    expect(convention).toBeLessThan(groups)
  })
})
