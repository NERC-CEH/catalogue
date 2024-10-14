import EditorView from '../EditorView'
import InputView from '../InputView'
import SelectView from '../SelectView'
import { KeywordVocabularyView,
  ParentView,
  ParentStringView,
  PredefinedParentView,
  RelationshipView,
  PredefinedSingleObjectView,
  TemporalExtentView,
  TextareaView,
  EnvironmentalDomainView,
  ResourceIdentifierView,
  ContactView,
  PurposeOfCollectionView,
  SupplementalLinkView
} from '../views'
import { MultipleDate, EnvironmentalDomain, PurposeOfCollection, Contact, Supplemental } from '../models'
import { BoundingBox, BoundingBoxView } from '../geometryMap'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'monitoringProgramme') }

    this.sections = [{
      label: 'General',
      title: 'General information',
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
          rows: 10,
          label: 'Description'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'objectives',
          label: 'Objectives',
          rows: 7
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'operatingPeriod',
          ModelType: MultipleDate,
          label: 'Operating period',
          ObjectInputView: TemporalExtentView
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'operationalStatus',
          label: 'Status',
          options: [
            { value: 'Unknown', label: 'Unknown' },
            { value: 'Active', label: 'Active' },
            { value: 'Inactive', label: 'Inactive' }
          ]
        })
      ]
    },
    {
      label: 'Location/coverage',
      title: 'Spatial coverage of the programme',
      views: [

        new PredefinedSingleObjectView({
          model: this.model,
          modelAttribute: 'boundingBox',
          ModelType: BoundingBox,
          label: 'Bounding box',
          ObjectInputView: BoundingBoxView,
          predefined: {
            England: {
              northBoundLatitude: 55.812,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -6.452
            },
            'Great Britain': {
              northBoundLatitude: 60.861,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -8.648
            },
            'Northern Ireland': {
              northBoundLatitude: 55.313,
              eastBoundLongitude: -5.432,
              southBoundLatitude: 54.022,
              westBoundLongitude: -8.178
            },
            Scotland: {
              northBoundLatitude: 60.861,
              eastBoundLongitude: -0.728,
              southBoundLatitude: 54.634,
              westBoundLongitude: -8.648
            },
            'United Kingdom': {
              northBoundLatitude: 60.861,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -8.648
            },
            Wales: {
              northBoundLatitude: 53.434,
              eastBoundLongitude: -2.654,
              southBoundLatitude: 51.375,
              westBoundLongitude: -5.473
            },
            World: {
              northBoundLatitude: 90.00,
              eastBoundLongitude: 180.00,
              southBoundLatitude: -90.00,
              westBoundLongitude: -180.00
            }
          }
        })
      ]
    },
    {
      label: 'Keywords/classification',
      title: 'Keywords and classification',
      views: [

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
          label: 'Parameters measured',
          ObjectInputView: KeywordVocabularyView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
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
            'Lead organisation - Environment Agency': {
              organisationName: 'Environment Agency',
              role: 'pointOfContact',
              email: 'enquiries@environment-agency.gov.uk',
              organisationIdentifier: 'https://ror.org/01zewfb16'
            },
            'Lead organisation - Natural England': {
              organisationName: 'Natural England',
              role: 'pointOfContact',
              email: 'enquiries@naturalengland.org.uk',
              organisationIdentifier: 'https://ror.org/00r66pz14'
            },
            'Lead organisation - Natural Resources Wales': {
              organisationName: 'Natural Resources Wales',
              role: 'pointOfContact',
              email: 'enquiries@naturalresourceswales.gov.uk',
              organisationIdentifier: 'https://ror.org/04x65hs26'
            },
            'Lead organisation - UKCEH': {
              organisationName: 'UK Centre for Ecology & Hydrology',
              role: 'pointOfContact',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55'
            },
            'Funder - Defra': {
              organisationName: 'Defra',
              role: 'funder',
              email: 'defra.helpline@defra.gsi.gov.uk',
              organisationIdentifier: 'https://ror.org/00tnppw48'
            },
            'Funder - NERC': {
              organisationName: 'Natural Environment Research Council',
              role: 'funder',
              organisationIdentifier: 'https://ror.org/02b5d8509'
            },
            'Funder - Scottish Government': {
              organisationName: 'Scottish Government',
              role: 'funder',
              organisationIdentifier: 'https://ror.org/04v2xmd71'
            },
            'Funder - Welsh Government': {
              organisationName: 'Welsh Government',
              role: 'funder',
              organisationIdentifier: 'https://ror.org/000wh6t45'
            }
          }
        })
      ]
    },
    {
      label: 'IDs and links',
      title: 'IDs and links',
      views: [

        new ParentView({
          model: this.model,
          modelAttribute: 'resourceIdentifiers',
          label: 'Identifiers',
          ObjectInputView: ResourceIdentifierView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'relationships',
          label: 'Relationships',
          ObjectInputView: RelationshipView,
          multiline: true,
          options: [
            { value: 'http://onto.ceh.ac.uk/EF#utilises', label: 'Uses (facility or network)' },
            { value: 'http://onto.ceh.ac.uk/EF#associatedWith', label: 'Related to' },
            { value: 'http://onto.ceh.ac.uk/EF#supersedes', label: 'Supersedes' },
            { value: 'http://onto.ceh.ac.uk/EF#triggers', label: 'Triggers (activity)' }
          ],
          helpText: `
<p>Links to other records</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'linksData',
          ModelType: Supplemental,
          multiline: true,
          label: 'Links to data',
          ObjectInputView: SupplementalLinkView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'linksOther',
          ModelType: Supplemental,
          multiline: true,
          label: 'Other links',
          ObjectInputView: SupplementalLinkView
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
