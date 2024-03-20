import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  DatasetReferenceDateView, KeywordView,
  LinkView,
  ParentView, RelationshipView, ResourceIdentifierView,
  SingleObjectView,
  TemporalExtentView,
  TextareaView
} from '../views'
import { MultipleDate } from '../models'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'publication') }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [

        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title',
          helpText: `
<p>Name of publication</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          rows: 13,
          label: 'Description',
          helpText: `
<p>Description of publication</p>
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'referenceDate',
          ModelType: MultipleDate,
          label: 'Reference Date',
          ObjectInputView: DatasetReferenceDateView,
          helpText: `
<p>Publication, creation & revision dates</p>
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'temporalExtent',
          ModelType: MultipleDate,
          label: 'Temporal Extent',
          ObjectInputView: TemporalExtentView,
          helpText: `
<p>Temporal Extent of publication</p>
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'access',
          label: 'Access',
          ObjectInputView: LinkView,
          helpText: `
<p>Access to publication</p>
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
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/cites', label: 'Cites' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/related', label: 'Related' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/supersedes', label: 'Supersedes' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces', label: 'Produces' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses', label: 'Uses' }
          ],
          helpText: `
<p>Relationships to other OSDP document types</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'resourceIdentifiers',
          label: 'Resource Identifiers',
          ObjectInputView: ResourceIdentifierView,
          helpText: `\
<p>A unique string or number used to identify the resource.</p>
<p> The codespace identifies the context in which the code is unique.</p>\
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
