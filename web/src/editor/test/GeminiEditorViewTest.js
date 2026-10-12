import _ from 'underscore'
import { GeminiEditorView } from '../src/editors'
import { EditorMetadata } from '../src'
import { Contact } from '../src/models'
import { ContactView } from '../src/views'

describe('GeminiEditorView', function () {
  function editorView (modelAttribute) {
    const view = new GeminiEditorView({ model: new EditorMetadata({ type: 'dataset' }) })
    return view.sections
      .flatMap(section => section.views)
      .find(v => v.data && v.data.modelAttribute === modelAttribute)
  }

  function relationshipOptions () {
    return editorView('relationships').data.options.map(option => option.value)
  }

  it('offers the monitoring facility a dataset was produced at', () => {
    // RelationshipView narrows the target search for this predicate to
    // monitoring facilities and networks
    expect(relationshipOptions()).toContain('https://digital.ceh.ac.uk/ontology/doo/utilises')
  })

  describe('Contributors section', () => {
    // dri-one #429: the section was commented out pending staff training, so
    // contributor roles exported to RDF and DataCite could never be entered
    function contributorView () {
      // Built the way ChildLargeView builds each entry, from the section's own options
      const contributors = editorView('contributors')
      const model = new Contact()
      const view = new ContactView(_.extend({}, contributors.data, { model, index: 0 }))
      return { view, model }
    }

    it('is offered in the editor', () => {
      expect(editorView('contributors')).toBeDefined()
    })

    it('tags new entries with the contributor role', () => {
      expect(contributorView().model.get('role')).toBe('contributor')
    })

    it('shows the contributor role select', () => {
      const { view } = contributorView()
      expect(view.$('.contributorRole').hasClass('d-none')).toBe(false)
      expect(view.$('select.contributorRole-select option').map((i, o) => o.value).get())
        .toEqual(jasmine.arrayContaining(['data-curator', 'workpackage-leader']))
    })
  })
})
