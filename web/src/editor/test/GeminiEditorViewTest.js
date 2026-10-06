import { GeminiEditorView } from '../src/editors'
import { EditorMetadata } from '../src'

describe('GeminiEditorView', function () {
  function relationshipOptions () {
    const view = new GeminiEditorView({ model: new EditorMetadata({ type: 'dataset' }) })
    const relationships = view.sections
      .flatMap(section => section.views)
      .find(v => v.data && v.data.modelAttribute === 'relationships')
    return relationships.data.options.map(option => option.value)
  }

  it('offers the monitoring facility a dataset was produced at', () => {
    // RelationshipView narrows the target search for this predicate to
    // monitoring facilities and networks
    expect(relationshipOptions()).toContain('https://digital.ceh.ac.uk/ontology/doo/utilises')
  })
})
