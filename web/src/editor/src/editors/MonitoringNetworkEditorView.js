import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  KeywordView,
  ParentView,
  RelationshipView,
  TextareaView
} from '../views'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'monitoringNetwork') }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [

        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title',
          helpText: `
<p>Name of Monitoring Facility</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          rows: 13,
          label: 'Description',
          helpText: `
<p>Description of Monitoring Facility</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Keywords',
          ObjectInputView: KeywordView,
          helpText: `
<p>Keywords for discovery</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'relationships',
          label: 'Relationships',
          ObjectInputView: RelationshipView,
          multiline: true,
          options: [
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/partOf', label: 'Part of' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces', label: 'Produces' }
          ],
          helpText: `
<p>Relationships to other document types</p>
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
