import { EditorView, InputView } from '../index'
import {
  ContactView,
  ParentView,
  SingleObjectView,
  PredefinedParentLargeView,
  RelationshipView,
  TextareaView, NoteView,
  SampleStorageLocationView,
  CheckboxView,
  ResourceIdentifierView, KeywordVocabularyView
} from '../views'
import {
  Contact
} from '../models'
export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'sample') }

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
          label: 'Contents'
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'storageLocation',
          label: 'Storage location',
          ObjectInputView: SampleStorageLocationView
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'archiveDate',
          label: 'Archive date',
          typeAttribute: 'date'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'reviewDate',
          label: 'Review date',
          typeAttribute: 'date'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'restrictions',
          label: 'Restrictions'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'containsPersonalData',
          label: 'Contains personal data',
        }),

        new PredefinedParentLargeView({
          model: this.model,
          ModelType: Contact,
          modelAttribute: 'contactPoints',
          label: 'Contacts',
          ObjectInputView: ContactView,
          multiline: true,
          roleDefault: 'pointOfContact',
          predefined: {
            'UKCEH Bangor': {
              organisationName: 'UK Centre for Ecology & Hydrology',
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

        new ParentView({
          model: this.model,
          modelAttribute: 'notes',
          label: 'Notes',
          ObjectInputView: NoteView,
          multiline: true
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'resourceIdentifiers',
          label: 'Identifiers',
          ObjectInputView: ResourceIdentifierView
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
