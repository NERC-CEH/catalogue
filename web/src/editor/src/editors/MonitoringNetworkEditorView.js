import EditorView from '../EditorView'
import InputView from '../InputView'
import SelectView from '../SelectView'
import {
  KeywordVocabularyView,
  ParentView,
  ParentStringView,
  PredefinedParentView,
  RelationshipView,
  TextareaView,
  ContactView,
  ResourceIdentifierView,
  SupplementalLinkView, EnvironmentalDomainView
} from '../views'
import { Contact, Supplemental, EnvironmentalDomain } from '../models'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'monitoringNetwork') }

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
          modelAttribute: 'pointsOfContact',
          label: 'Contacts',
          ObjectInputView: ContactView,
          roleDefault: 'pointOfContact',
          multiline: true,
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
            { value: 'http://onto.ceh.ac.uk/EF#associatedWith', label: 'Related to' },
            { value: 'http://onto.ceh.ac.uk/EF#supersedes', label: 'Supersedes' },
            { value: 'http://onto.ceh.ac.uk/EF#narrower', label: 'Has child network' }
          ],
          helpText: `
<p>Relationships to other records</p>
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
