import { EditorView, InputView } from '../index'
import {
  ContactView,
  KeywordView,
  OnlineResourceSimpleView,
  ParentView,
  PredefinedParentView,
  RelationshipView,
  TextareaView,
  AdditionalInfoView
} from '../views'
import {
  Contact
} from '../models'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'methodRecord') }

    this.sections = [{
      label: 'General',
      title: 'General',
      views: [

        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Name'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description',
          rows: 10
        }),

        new PredefinedParentView({
          model: this.model,
          ModelType: Contact,
          modelAttribute: 'contacts',
          label: 'Contact',
          ObjectInputView: ContactView,
          multiline: true,
          predefined: {
            'UKCEH Bangor': {
              organisationName: 'UK Centre for Ecology & Hydrology',
              role: 'pointOfContact',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55',
              address: {
                city: 'Bangor'
              }
            },
            'UKCEH Edinburgh': {
              organisationName: 'UK Centre for Ecology & Hydrology',
              role: 'pointOfContact',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55',
              address: {
                city: 'Edinburgh'
              }
            },
            'UKCEH Lancaster': {
              organisationName: 'UK Centre for Ecology & Hydrology',
              role: 'pointOfContact',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55',
              address: {
                city: 'Lancaster'
              }
            },
            'UKCEH Wallingford': {
              organisationName: 'UK Centre for Ecology & Hydrology',
              role: 'pointOfContact',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55',
              address: {
                city: 'Wallingford'
              }
            }
          }
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Keywords',
          ObjectInputView: KeywordView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'onlineResources',
          label: 'Online resources',
          ObjectInputView: OnlineResourceSimpleView,
          multiline: true
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'relationships',
          label: 'Relationships',
          ObjectInputView: RelationshipView,
          multiline: true,
          options: [
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/related', label: 'Related' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces', label: 'Produces' },
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses', label: 'Uses' }
          ]
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'additionalInfo',
          multiline: true,
          label: 'Additional information',
          ObjectInputView: AdditionalInfoView,
          helpText: `\
<p>Enter information as key-value pairs.</p>\
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
