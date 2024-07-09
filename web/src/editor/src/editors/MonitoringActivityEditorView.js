import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  KeywordVocabularyView,
  ParentView,
  PredefinedParentView,
  RelationshipView,
  TemporalExtentView,
  TextareaView,
  ContactView,
  ParentStringView,
  EnvironmentalDomainView,
  PurposeOfCollectionView,
  ResourceIdentifierView,
  SupplementalLinkView,
  PredefinedSingleObjectView
} from '../views'
import { MultipleDate, EnvironmentalDomain, PurposeOfCollection, Contact, Supplemental } from '../models'
import { BoundingBox, BoundingBoxView } from '../geometryMap'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'monitoringActivity') }

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
        })

      ]
    },
    {
      label: 'Location/coverage',
      title: 'Spatial coverage of the activity',
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
              westBoundLongitude: -6.452,
              extentName: 'England',
              extentUri: 'http://sws.geonames.org/6269131'
            },
            'Great Britain': {
              northBoundLatitude: 60.861,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -8.648,
              extentName: 'Great Britain'
            },
            'Northern Ireland': {
              northBoundLatitude: 55.313,
              eastBoundLongitude: -5.432,
              southBoundLatitude: 54.022,
              westBoundLongitude: -8.178,
              extentName: 'Northern Ireland',
              extentUri: 'http://sws.geonames.org/2641364'
            },
            Scotland: {
              northBoundLatitude: 60.861,
              eastBoundLongitude: -0.728,
              southBoundLatitude: 54.634,
              westBoundLongitude: -8.648,
              extentName: 'Scotland',
              extentUri: 'http://sws.geonames.org/2638360'
            },
            'United Kingdom': {
              northBoundLatitude: 60.861,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -8.648,
              extentName: 'United Kingdom',
              extentUri: 'http://sws.geonames.org/2635167'
            },
            Wales: {
              northBoundLatitude: 53.434,
              eastBoundLongitude: -2.654,
              southBoundLatitude: 51.375,
              westBoundLongitude: -5.473,
              extentName: 'Wales',
              extentUri: 'http://sws.geonames.org/2634895'
            },
            World: {
              northBoundLatitude: 90.00,
              eastBoundLongitude: 180.00,
              southBoundLatitude: -90.00,
              westBoundLongitude: -180.00
            }
          },
          helpText: `<p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive)
            it is recommended that you generalise the location.</p>`
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
            { value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses', label: 'Uses' }
          ],
          helpText: `
<p>Facilities and networks used by this activity</p>
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
