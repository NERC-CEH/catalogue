import { EditorView, InputView } from '../index'
import {
  ContactView,
  SimpleLinkView,
  ParentStringView,
  ParentView,
  PredefinedParentView,
  PredefinedParentLargeView,
  RelationshipView,
  TextareaView,
  FundingView
} from '../views'
import {
  Contact,
  Funding
} from '../models'
export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'researchActivity') }

    this.sections = [{
      label: 'General',
      title: 'General',
      views: [

        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Name'
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'alternateNames',
          label: 'Alternative names'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description',
          rows: 6
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'onlineResources',
          label: 'Online resources',
          ObjectInputView: SimpleLinkView,
          multiline: true
        })

      ]
    },
    {
      label: 'Contacts',
      title: 'Contacts',
      views: [
        new PredefinedParentLargeView({
          model: this.model,
          ModelType: Contact,
          modelAttribute: 'contactPoints',
          label: 'Contact point',
          ObjectInputView: ContactView,
          multiline: true,
          roleDefault: 'pointOfContact',
          predefined: {
            'UKCEH Bangor': {
              organisationName: 'UK Centre for Ecology & Hydrology',
              role: 'pointOfContact',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55',
              address: {
                deliveryPoint: 'Environment Centre Wales, Deiniol Road',
                postalCode: 'LL57 2UW',
                city: 'Bangor',
                administrativeArea: 'Gwynedd',
                country: 'United Kingdom'
              }
            },
            'UKCEH Edinburgh': {
              organisationName: 'UK Centre for Ecology & Hydrology',
              role: 'pointOfContact',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55',
              address: {
                deliveryPoint: 'Bush Estate',
                postalCode: 'EH26 0QB',
                city: 'Penicuik',
                administrativeArea: 'Midlothian',
                country: 'United Kingdom'
              }
            },
            'UKCEH Lancaster': {
              organisationName: 'UK Centre for Ecology & Hydrology',
              role: 'pointOfContact',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55',
              address: {
                deliveryPoint: 'Lancaster Environment Centre, Library Avenue, Bailrigg',
                postalCode: 'LA1 4AP',
                city: 'Lancaster',
                administrativeArea: 'Lancashire',
                country: 'United Kingdom'
              }
            },
            'UKCEH Wallingford': {
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
        }),

        new PredefinedParentLargeView({
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
      label: 'Relationships',
      title: 'Relationships',
      views: [

        new ParentView({
          model: this.model,
          modelAttribute: 'relationships',
          label: 'Relationships',
          ObjectInputView: RelationshipView,
          multiline: true,
          options: [
            {
              value: 'http://purl.org/cerif/frapo/hasOutput',
              label: 'Has output'
            },
            {
              value: 'http://purl.org/dc/terms/relation',
              label: 'Relation'
            }
          ]
        })

      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
