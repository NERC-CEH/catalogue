import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  KeywordView,
  ParentView,
  RelationshipView,
  SingleObjectView,
  TemporalExtentView,
  TextareaView,
  RelatedRecordView
} from '../views'
import { MultipleDate } from '../models'
import {
  Geometry,
  GeometryView
} from '../geometryMap'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) {
      this.model.set('type', 'monitoringFacility')
    }

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

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'geometry',
          ModelType: Geometry,
          label: 'Geometry',
          ObjectInputView: GeometryView,
          helpText: `
<p>Geometry of Monitoring Facility</p>
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'facilityType',
          label: 'Type',
          helpText: `
<p>Type of Monitoring Facility</p>
`
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'temporalExtent',
          ModelType: MultipleDate,
          label: 'Temporal Extent',
          ObjectInputView: TemporalExtentView,
          helpText: `
<p>Temporal Extent of Monitoring Facility</p>
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
<p>Relationships to other OSDP document types</p>
`
        })

      ]
    },
    {
      label: 'ID & relationships',
      title: 'Identifiers and links to related resources',
      views: [
        new ParentView({
          model: this.model,
          modelAttribute: 'relatedRecords',
          label: 'Related records',
          ObjectInputView: RelatedRecordView,
          multiline: true,
          helpText: `
<p>This is to link related datasets,etc which are in <i>this</i> catalogue. Externally hosted datasets can be linked using <strong>Supplemental</strong> &gt; <strong>Additional links</strong> &gt; <strong>Related dataset</strong></p>
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
