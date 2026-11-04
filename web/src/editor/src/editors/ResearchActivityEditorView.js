import EditorView from '../EditorView'
import InputView from '../InputView'
import SelectView from '../SelectView'
import {
  KeywordVocabularyView,
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
  SupplementalLinkView,
  FundingView
} from '../views'
import { MultipleDate, EnvironmentalDomain, PurposeOfCollection, Contact, Supplemental, Funding } from '../models'
import { BoundingBox, BoundingBoxView } from '../geometryMap'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'researchActivity') }

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
          modelAttribute: 'alternateNames',
          label: 'Alternative name(s)'
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'activityType',
          label: 'Type of activity',
          options: [
            { value: 'earthObservation', label: 'Earth observation', description: 'Research whose primary objective is the acquisition and use of remotely sensed observations from satellites, aircraft, drones, or similar platforms to study environmental processes and change' },
            { value: 'environmentalMonitoring', label: 'Environmental monitoring scheme' },
            { value: 'experimentalResearch', label: 'Experimental research' },
            { value: 'instrumentedNetwork', label: 'Instrumented network' },
            { value: 'modelling', label: 'Modelling' },
            { value: 'periodicSurvey', label: 'Periodic survey' }
          ]
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          rows: 5,
          label: 'Description'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'objectives',
          label: 'Objectives'
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
      label: 'Spatial coverage',
      title: 'Spatial coverage',
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
      label: 'Contacts',
      title: 'Contacts',
      views: [

        new PredefinedParentView({
          model: this.model,
          ModelType: Contact,
          modelAttribute: 'contacts',
          label: 'Contacts',
          ObjectInputView: ContactView,
          multiline: true,
          roleDefault: 'pointOfContact',
          predefined: {
            'Environment Agency': {
              organisationName: 'Environment Agency',
              email: 'enquiries@environment-agency.gov.uk',
              organisationIdentifier: 'https://ror.org/01zewfb16'
            },
            'Natural England': {
              organisationName: 'Natural England',
              email: 'enquiries@naturalengland.org.uk',
              organisationIdentifier: 'https://ror.org/00r66pz14'
            },
            'Natural Resources Wales': {
              organisationName: 'Natural Resources Wales',
              email: 'enquiries@naturalresourceswales.gov.uk',
              organisationIdentifier: 'https://ror.org/04x65hs26'
            },
            UKCEH: {
              organisationName: 'UK Centre for Ecology & Hydrology',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55'
            }
          }
        }),

        new PredefinedParentView({
          model: this.model,
          ModelType: Contact,
          modelAttribute: 'contributors',
          label: 'Contributors',
          ObjectInputView: ContactView,
          multiline: true,
          roleDefault: 'contributor',
          predefined: {
            UKCEH: {
              organisationName: 'UK Centre for Ecology & Hydrology',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55'
            },
            Unaffiliated: {
              organisationName: 'Unaffiliated'
            }
          }
        }),

        new ParentView({
          model: this.model,
          ModelType: Contact,
          modelAttribute: 'partners',
          label: 'Partners',
          ObjectInputView: ContactView,
          multiline: true,
          roleoptions: [
            { value: 'funder', label: 'Funder' },
            { value: 'stakeholder', label: 'Stakeholder' },
            { value: 'user', label: 'User' }
          ]
        })
      ]
    },
    {
      label: 'Funding',
      title: 'Funding',
      views: [

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'funding',
          ModelType: Funding,
          multiline: true,
          label: 'Funding',
          ObjectInputView: FundingView,
          predefined: {
            BBSRC: {
              funderName: 'Biotechnology and Biological Sciences Research Council',
              funderIdentifier: 'https://ror.org/00cwqg982'
            },
            Defra: {
              funderName: 'Department for Environment Food and Rural Affairs',
              funderIdentifier: 'https://ror.org/00tnppw48'
            },
            EPSRC: {
              funderName: 'Engineering and Physical Sciences Research Council',
              funderIdentifier: 'https://ror.org/0439y7842'
            },
            ESRC: {
              funderName: 'Economic and Social Research Council',
              funderIdentifier: 'https://ror.org/03n0ht308'
            },
            'Innovate UK': {
              funderName: 'Innovate UK',
              funderIdentifier: 'https://ror.org/05ar5fy68'
            },
            MRC: {
              funderName: 'Medical Research Council',
              funderIdentifier: 'https://ror.org/03x94j517'
            },
            NERC: {
              funderName: 'Natural Environment Research Council',
              funderIdentifier: 'https://ror.org/02b5d8509'
            },
            STFC: {
              funderName: 'Science and Technology Facilities Council',
              funderIdentifier: 'https://ror.org/057g20z61'
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
          ObjectInputView: KeywordVocabularyView,
          vocabs: {
            ukceh: ['envThes', 'gemet'],
            eidc: ['envThes', 'gemet'],
            ukeof: ['envThes', 'gemet']
          },
          multiline: true
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Keywords',
          ObjectInputView: KeywordVocabularyView,
          vocabs: {
            ukceh: ['envThes', 'gemet'],
            eidc: ['envThes', 'gemet'],
            ukeof: ['envThes', 'gemet']
          },
          multiline: true
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
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'relationships',
          label: 'Relationships',
          ObjectInputView: RelationshipView,
          multiline: true,
          options: [
            { value: 'https://digital.ceh.ac.uk/ontology/doo/utilises', label: 'Uses (facility or network)' },
            { value: 'http://purl.org/dc/terms/hasPart', label: 'Has part' },
            { value: 'http://purl.org/dc/terms/relation', label: 'Related to' },
            { value: 'http://purl.org/dc/terms/replaces', label: 'Supersedes' }
          ],
          helpText: `
<p>Links to other records</p>
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
