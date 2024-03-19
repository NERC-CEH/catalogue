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
  ContactView,
  ParentStringView,
  EnvironmentalDomainView,
  PurposeOfCollectionView
} from '../views'
import { MultipleDate, EnvironmentalDomain, PurposeOfCollection, Contact } from '../models'
import { BoundingBox, BoundingBoxView } from '../geometryMap'

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
          label: 'Name'
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'alternateTitles',
          label: 'Alternative name(s)'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          rows: 13,
          label: 'Description'
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'temporalExtent',
          ModelType: MultipleDate,
          label: 'Temporal Extent',
          ObjectInputView: TemporalExtentView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'environmentalDomain',
          ModelType: EnvironmentalDomain,
          label: 'Environmental domain',
          ObjectInputView: EnvironmentalDomainView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'purposeOfCollection',
          ModelType: PurposeOfCollection,
          label: 'Purpose of collection',
          ObjectInputView: PurposeOfCollectionView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywordsParameters',
          label: 'Paremeters measured',
          ObjectInputView: KeywordVocabularyView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywordsOther',
          label: 'Keywords',
          ObjectInputView: KeywordVocabularyView
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
      label: 'Location',
      title: 'Location',
      views: [


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
<p>Relationships to other document types</p>
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
