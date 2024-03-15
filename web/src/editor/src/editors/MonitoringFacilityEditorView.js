import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  KeywordVocabularyView,
  ParentView,
  PredefinedParentView,
  RelationshipView,
  SingleObjectView,
  TemporalExtentView,
  TextareaView,
  FacilityTypeView,
  EnvironmentalDomainView,
  ParentStringView,
  SupplementalTextView,
  CheckboxView
} from '../views'
import { MultipleDate, FacilityType, EnvironmentalDomain, Supplemental } from '../models'
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
          label: 'Name',
          helpText: `
<p>Name of Monitoring Facility</p>
`
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'alternateTitles',
          label: 'Alternative name(s)'
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'facilityType',
          ModelType: FacilityType,
          label: 'Type of facility',
          ObjectInputView: FacilityTypeView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'environmentalDomain',
          ModelType: EnvironmentalDomain,
          label: 'Environmental domain',
          ObjectInputView: EnvironmentalDomainView
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
          modelAttribute: 'supplemental',
          ModelType: Supplemental,
          multiline: true,
          label: 'Additional information',
          ObjectInputView: SupplementalTextView
        })
      ]
    },
    {
      label: 'Location',
      title: 'Location',
      views: [
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

        new CheckboxView({
          model: this.model,
          modelAttribute: 'geometryRepresentative',
          label: 'Geometry is representative?'

        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'mobile',
          label: 'Facility is mobile?'
        })
      ]
    },
    {
      label: 'Contacts',
      title: 'Contacts',
      views: [
        new PredefinedParentView({
          model: this.model,
          ModelType: Contact,
          modelAttribute: 'responsibleParties',
          label: 'Contacts',
          ObjectInputView: ContactView,
          multiline: true,
          predefined: {
            'Point of contact - UKCEH': {
              organisationName: 'UK Centre for Ecology & Hydrology',
              role: 'pointOfContact',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55',
              address: {
                deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford',
                postalCode: 'OX10 8BB',
                city: 'Wallingford',
                administrativeArea: 'Oxfordshire',
                country: 'United Kingdom'
              }
            }
          }
        })
      ]
      },
      {
        label: 'Supplemental',
        title: 'Additional information and funding',
        views: [

          new SingleObjectView({
            model: this.model,
            modelAttribute: 'temporalExtent',
            ModelType: MultipleDate,
            label: 'Temporal extent',
            ObjectInputView: TemporalExtentView,
            helpText: `
<p>Temporal Extent of Monitoring Facility</p>
`
          }),

          new ParentView({
            model: this.model,
            modelAttribute: 'keywords',
            label: 'Keywords',
            ObjectInputView: KeywordVocabularyView,
            multiline: true
          }),

          new ParentView({
            model: this.model,
            modelAttribute: 'relationships',
            label: 'Relationships',
            ObjectInputView: RelationshipView,
            multiline: true,
            options: [
              { value: 'http://purl.org/voc/ef#belongsTo', label: 'Belongs To' }
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
