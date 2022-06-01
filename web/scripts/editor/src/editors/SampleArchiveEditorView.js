import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  FundingView,
  KeywordView,
  OnlineLinkView,
  ParentView,
  PointOfContactView,
  PredefinedParentView,
  SaPhysicalStateView,
  SaSpecimenTypeView,
  SaTaxaView,
  SaTissueView,
  SingleObjectView,
  TemporalExtentView,
  TextareaView
} from '../views'
import { MultipleDate, PointOfContact, SaPhysicalState, SaSpecimenType, SaTaxa, SaTissue } from '../models'
import BoundingBoxView from '../geometryMap/BoundingBoxView'
import BoundingBox from '../geometryMap/BoundingBox'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'sampleArchive') }

    this.sections = [{
      label: 'About',
      title: 'About the archive',
      views: [
        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description',
          rows: 7,
          helpText: `
<p>An brief overview that gives details about the sample collection</p>
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'website',
          label: 'Website address',
          helpText: `
<p>The archive's website</p>
`
        }),

        new ParentView({
          model: this.model,
          ModelType: PointOfContact,
          modelAttribute: 'archiveLocations',
          label: 'Archive locations',
          ObjectInputView: PointOfContactView,
          multiline: true,
          helpText: `
<p>Location(s) of the archiving facility</p>
`
        }),

        new ParentView({
          model: this.model,
          ModelType: PointOfContact,
          modelAttribute: 'archiveContacts',
          label: 'Contacts',
          ObjectInputView: PointOfContactView,
          multiline: true,
          helpText: `
<p>Person/organisation to contact for more information about or access to the sample archive</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'lineage',
          label: 'Lineage',
          rows: 5,
          helpText: `
<p>An overview of how samples are collected and any Quality Control or Quality Assurance</p>
`
        })
      ]
    },
    {
      label: 'Availability and access',
      title: 'Availability and access',
      views: [

        new TextareaView({
          model: this.model,
          modelAttribute: 'availability',
          label: 'Availability',
          rows: 5,
          helpText: `
<p>Information about how readily available the samples are, who is allowed to have access and how to gain access</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'accessRestrictions',
          label: 'Access restrictions',
          rows: 5,
          helpText: `
<p>An overview of any restrictions that will apply (usually things like Intellectual Property Rights and Terms and Conditions)</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'storage',
          label: 'Storage',
          rows: 5,
          helpText: `
<p>An overview of how the samples are stored to help a potential sample user understand what will be required if they request a sample</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'healthSafety',
          label: 'Health and safety',
          rows: 5,
          helpText: `
<p>Any information users need to know regarding Health and Safety when they are considering using samples from the archive</p>
`
        })

      ]
    },
    {
      label: 'Additional info',
      title: 'Additional information',
      views: [

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'funding',
          label: 'Funding',
          ObjectInputView: FundingView,
          multiline: true,
          helpText: `
<p>Details of funding organisations</p>
`,
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
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'temporalExtent',
          ModelType: MultipleDate,
          label: 'Temporal extent',
          ObjectInputView: TemporalExtentView,
          helpText: `
<p>Start date is mandatory and it represents the earliest sample in the archive - if not precisely known, it  may be approximated. End date is optional, if it is provided it represents the last sample in the archive.</p>
`
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'boundingBoxes',
          ModelType: BoundingBox,
          label: 'Spatial coverage',
          ObjectInputView: BoundingBoxView,
          multiline: true,
          // These bounding box values were copied from terraCatalog
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
          },
          helpText: `
        <p>A bounding box showing the area that the archive covers. It will encompass the remit of the archive, which may be larger than that represented by the samples actually in the archive. It is represented by north, south, east and west in decimal degrees (WGS84).</p><p>If you don't know the bounding box values, click on the map to place a rectangle at the approximate location and adjust by dragging it or changing its shape using the handles on the rectangle.</p>
        `
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'onlineResources',
          label: 'Additional Resources',
          ObjectInputView: OnlineLinkView,
          listAttribute: `\
<option value='Website' />
<option value='browseGraphic' />\
`,
          helpText: `
<p>A list of websites that may be of use to the user</p>
`
        })

      ]
    },
    {
      label: 'Classification',
      title: 'Cataloguing and classification',
      views: [

        new ParentView({
          model: this.model,
          ModelType: SaTaxa,
          modelAttribute: 'taxa',
          label: 'Taxa',
          ObjectInputView: SaTaxaView
        }),

        new ParentView({
          model: this.model,
          ModelType: SaPhysicalState,
          modelAttribute: 'physicalStates',
          label: 'Physical state',
          ObjectInputView: SaPhysicalStateView
        }),

        new ParentView({
          model: this.model,
          ModelType: SaSpecimenType,
          modelAttribute: 'specimenTypes',
          label: 'Specimen type',
          ObjectInputView: SaSpecimenTypeView
        }),

        new ParentView({
          model: this.model,
          ModelType: SaTissue,
          modelAttribute: 'tissues',
          label: 'Tissue',
          ObjectInputView: SaTissueView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Other keywords',
          ObjectInputView: KeywordView,
          helpText: `
<p>A list of words and phrases that help to identify and describe the archive - useful for improving search results</p>
`
        })
      ]
    },
    {
      label: 'Metadata',
      title: 'Metadata',
      views: [

        new ParentView({
          model: this.model,
          ModelType: PointOfContact,
          modelAttribute: 'metadataContacts',
          label: 'Record owner',
          ObjectInputView: PointOfContactView,
          multiline: true,
          helpText: `
<p>Person responsible for maintaining this metadata record</p>
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'id',
          label: 'File identifier',
          readonly: true
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'uri',
          label: 'URL',
          readonly: true
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'metadataDate',
          label: 'Metadata date',
          readonly: true,
          helpText: `
<p>Date and time the record was last updated (generated automatically when you save a record and not editable)</p>
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
