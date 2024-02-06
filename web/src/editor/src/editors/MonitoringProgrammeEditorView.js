import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  KeywordView,
  ParentView,
  RelationshipView,
  SingleObjectView,
  TemporalExtentView,
  TextareaView
} from '../views'
import { MultipleDate } from '../models'
import BoundingBoxView from '../geometryMap/BoundingBoxView'
import BoundingBox from '../geometryMap/BoundingBox'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'monitoringProgramme') }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [

        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title',
          helpText: `
<p>Name of Monitoring Programme</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          rows: 13,
          label: 'Description',
          helpText: `
<p>Description of Monitoring Programme</p>
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'temporalExtent',
          ModelType: MultipleDate,
          label: 'Temporal Extent',
          ObjectInputView: TemporalExtentView,
          helpText: `
<p>Temporal Extent of Monitoring Programme</p>
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'boundingBox',
          ModelType: BoundingBox,
          label: 'Bounding Box',
          ObjectInputView: BoundingBoxView,
          helpText: `
                <p>Bounding Box of Monitoring Programme</p>
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
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/associatedWith', label: 'Associated with' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/owns', label: 'Owns' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/setupFor', label: 'Setup for' }
          ],
          helpText: `
<p>Relationships to other OSDP document types</p>
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
