import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  KeywordView,
  ParametersMeasuredView,
  ParentView,
  RelationshipView,
  SingleObjectView, TemporalExtentView, TextareaView
} from '../views'
import { MultipleDate } from '../models'
import BoundingBoxView from '../geometryMap/BoundingBoxView'
import BoundingBox from '../geometryMap/BoundingBox'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'monitoringActivity') }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [

        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Name',
          helpText: `
<p>Name of Monitoring Activity</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          rows: 13,
          label: 'Description',
          helpText: `
<p>Description of Monitoring Activity</p>
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'temporalExtent',
          ModelType: MultipleDate,
          label: 'Temporal Extent',
          ObjectInputView: TemporalExtentView,
          helpText: `
<p>Temporal Extent of Monitoring Activity</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'parametersMeasured',
          label: 'Parameters Measured',
          ObjectInputView: ParametersMeasuredView,
          multiline: true,
          helpText: `
<p>Parameters measured as part of Monitoring Activity</p>
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

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'boundingBox',
          ModelType: BoundingBox,
          label: 'Bounding Box',
          ObjectInputView: BoundingBoxView,
          helpText: `
                <p>Bounding Box of Monitoring Activity</p>
                `
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'relationships',
          label: 'Relationships',
          ObjectInputView: RelationshipView,
          multiline: true,
          options: [
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces', label: 'Produces' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/setupFor', label: 'Setup for' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses', label: 'Uses' }
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
