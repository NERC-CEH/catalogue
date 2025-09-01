import EditorView from '../EditorView'
import InputView from '../InputView'
import SelectView from '../SelectView'
import {
  KeywordVocabularyView,
  ParentView,
  PredefinedParentView,
  RelationshipView,
  SingleObjectView,
  TemporalExtentView,
  TextareaView,
  ContactView,
  FacilityTypeView,
  EnvironmentalDomainView,
  ResourceIdentifierView,
  ParentStringView,
  CheckboxView,
  AdditionalInfoView
} from '../views'
import { MultipleDate, FacilityType, EnvironmentalDomain, Contact } from '../models'
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

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'facilityType',
          ModelType: FacilityType,
          label: 'Type of facility',
          ObjectInputView: FacilityTypeView
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          rows: 10,
          label: 'Description'
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

        new CheckboxView({
          model: this.model,
          modelAttribute: 'geometryRepresentative',
          label: 'Geometry is representative?'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'mobile',
          label: 'Facility is mobile?'
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'additionalInfo',
          multiline: true,
          label: 'Additional information',
          ObjectInputView: AdditionalInfoView,
          helpText: `
<p>Enter information as key-value pairs.</p>
`
        })
      ]
    },
    {
      label: 'Keywords/classification',
      title: 'Keywords',
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
          modelAttribute: 'keywordsParameters',
          label: 'Parameters measured',
          ObjectInputView: KeywordVocabularyView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Keywords',
          ObjectInputView: KeywordVocabularyView,
          multiline: true
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
          modelAttribute: 'pointsOfContact',
          label: 'Point of contact',
          ObjectInputView: ContactView,
          multiline: true,
          roleDefault: 'pointOfContact',
          predefined: {
            UKCEH: {
              organisationName: 'UK Centre for Ecology & Hydrology',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55'
            },
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
            SEPA: {
              organisationName: 'Scottish Environment Protection Agency',
              organisationIdentifier: 'https://ror.org/01kxjy285'
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
            { value: 'siteowner', label: 'Site owner' },
            { value: 'stakeholder', label: 'Stakeholder' },
            { value: 'user', label: 'User' }
          ]
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
            { value: 'http://onto.ceh.ac.uk/EF#belongsTo', label: 'Belongs to' },
            { value: 'http://onto.ceh.ac.uk/EF#associatedWith', label: 'Related to' },
            { value: 'http://onto.ceh.ac.uk/EF#supersedes', label: 'Supersedes' },
            { value: 'http://onto.ceh.ac.uk/EF#narrower', label: 'Has child facility' }
          ],
          helpText: `
<p>Relationships to other records</p>
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
