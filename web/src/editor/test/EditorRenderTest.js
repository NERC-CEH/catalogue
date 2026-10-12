import $ from 'jquery'
import { GeminiEditorView, LinkEditorView } from '../src/editors'
import { EditorMetadata } from '../src'

/**
 * EditorView used to render every section view a second time (`append(view.render().el)`), which
 * replaced whatever a view had built during initialize. Saved contacts, contributors and keywords
 * vanished from the editor (EDS-2961), and `$(document).ready` wrappers hid that by delaying the
 * work by a tick. These specs read the editor straight after construction, with no tick, so neither
 * the second render nor the delay can come back unnoticed. See NERC-CEH/dri-one#451.
 */
describe('Opening a saved record in the editor', () => {
  const contributors = [
    { role: 'contributor', organisationName: 'Org A', contributorRole: 'data-curator' },
    { role: 'contributor', organisationName: 'Org B', contributorRole: 'researcher' }
  ]

  afterEach(() => {
    $('body').removeClass('edit-mode')
    $('#search').removeClass('search')
  })

  function geminiEditor (attributes) {
    return new GeminiEditorView({ model: new EditorMetadata(Object.assign({ type: 'dataset' }, attributes)) })
  }

  function sectionView (editor, modelAttribute) {
    return editor.sections
      .flatMap(section => section.views)
      .find(view => view.data && view.data.modelAttribute === modelAttribute)
  }

  it('shows the saved contributors', () => {
    const contributorsView = sectionView(geminiEditor({ contributors }), 'contributors')

    const rows = contributorsView.$('.existing').children()
    expect(rows.length).toBe(2)
    expect(rows.find('select.contributorRole-select').map((i, el) => $(el).val()).get())
      .toEqual(['data-curator', 'researcher'])
  })

  it('makes the saved contributors reorderable', () => {
    const contributorsView = sectionView(geminiEditor({ contributors }), 'contributors')

    expect(contributorsView.$('.existing').hasClass('ui-sortable')).toBe(true)
  })

  it('shows a single-object field inside its section', () => {
    const resourceTypeView = sectionView(geminiEditor({}), 'resourceType')

    expect(resourceTypeView.objectInputView).not.toBeNull()
    expect($.contains(resourceTypeView.el, resourceTypeView.objectInputView.el)).toBe(true)
  })

  it('shows the saved service operations', () => {
    const service = { containsOperations: [{ operationName: 'GetMap' }, { operationName: 'GetCapabilities' }] }
    const serviceView = sectionView(geminiEditor({ service }), 'service')

    const operations = serviceView.$('.dataentry .existing').first().children()
    expect(operations.length).toBe(2)
  })

  it('does not search a null catalogue for documents to link to', () => {
    spyOn($, 'getJSON').and.returnValue($.Deferred().promise())

    new LinkEditorView({ model: new EditorMetadata({ type: 'link' }) }) // eslint-disable-line no-new

    const urls = $.getJSON.calls.allArgs().map(args => args[0])
    expect(urls.filter(url => url.startsWith('/null/'))).toEqual([])
  })
})
