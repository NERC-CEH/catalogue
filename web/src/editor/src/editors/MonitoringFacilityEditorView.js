import EditorView from '../EditorView'
import InputView from '../InputView'
import SelectView from '../SelectView'
import {KeywordVocabularyView,
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
  CheckboxView
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
            { value: 'http://onto.ceh.ac.uk/EF#belongsTo', label: 'Belongs to' },
            { value: 'http://onto.ceh.ac.uk/EF#associatedWith', label: 'Related to' },
            { value: 'http://onto.ceh.ac.uk/EF#supersedes', label: 'Supersedes' }
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
