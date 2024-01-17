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
import BoundingBox from '../geometryMap/BoundingBox'
import BoundingBoxView from '../geometryMap/BoundingBoxView'

export default EditorView.extend({

    initialize () {
        if (!this.model.has('type')) { this.model.set('type', 'monitoringFacility') }

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

                new InputView({
                    model: this.model,
                    modelAttribute: 'facilityType',
                    label: 'Type',
                    helpText: `
<p>Type of Monitoring Facility</p>
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

                new SingleObjectView({
                    model: this.model,
                    modelAttribute: 'boundingBox',
                    ModelType: BoundingBox,
                    label: 'Bounding Box',
                    ObjectInputView: BoundingBoxView,
                    helpText: `
                <p>Bounding Box of Monitoring Facility</p>
                `
                }),

                //         new GeometryView({
                //           model: this.model,
                //           modelAttribute: 'geometry',
                //           label: 'Geometry',
                //           helpText: `
                // <p>Geometry of Monitoring Facility</p>
                // `
                //         }),

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
        }
        ]

        return EditorView.prototype.initialize.apply(this)
    }
})
