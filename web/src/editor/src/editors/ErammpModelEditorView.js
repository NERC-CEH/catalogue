import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  CheckboxView, ContactView, ErammpModelInputView, ErammpModelOutputView, KeywordView, OnlineLinkView,
  ParentStringView,
  ParentView, PredefinedParentView,
  TextareaView
} from '../views'
import { Contact } from '../models'
import BoundingBox from '../geometryMap/BoundingBox'
import BoundingBoxView from '../geometryMap/BoundingBoxView'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'erammpModel') }

    this.sections = [{
      label: 'General',
      title: 'General',
      views: [
        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Name of model'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'version',
          label: 'Version'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description',
          rows: 5,
          helpText: `
<p>A short description that gives details about the model. A well-written description will ensure the record appears appropriately in searches.</p>
`
        }),

        new ParentView({
          model: this.model,
          ModelType: Contact,
          modelAttribute: 'contacts',
          label: 'Contacts',
          ObjectInputView: ContactView,
          multiline: true,
          helpText: `
<p>The contact(s) responsible for this model and who can be contacted if there are questions about it.  A <b>named</b> person is recommended</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'ipr',
          label: 'IPR',
          rows: 5,
          helpText: `
<p>Are there any IPR issues associated with making internal variables and/or outputs from the models available?</p>
`
        })
      ]
    },
    {
      label: 'Spatial',
      title: 'Spatial',
      views: [

        new CheckboxView({
          model: this.model,
          modelAttribute: 'spatiallyExplicit',
          label: 'The model is spatially explicit'
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'boundingBoxes',
          ModelType: BoundingBox,
          label: 'Spatial extents',
          ObjectInputView: BoundingBoxView,
          predefined: {
            'England (or England & Wales)': {
              northBoundLatitude: 55.812,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -6.452
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
            'UK (or Great Britain)': {
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
          multiline: true,
          helpText: `\
                <p>A bounding box showing the area that the archive covers. It will encompass the remit of the archive, which may be larger than that represented by the samples actually in the archive. It is represented by north, south, east and west in decimal degrees (WGS84).</p>
                <p>Enter the values, or click on the map to draw a  rectangle at the approximate location.</p>\
                `
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'spatialResolution',
          label: 'Maximum spatial resolution',
          placeholderAttribute: 'Maximum  resolution at which you would be comfortable applying the model',
          helpText: `
<p>What is the maximum spatial resolution (most detailed, smallest pixel size) at which you would be comfortable applying the model?</p>
`
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'calibratedForWales',
          label: 'The model is calibrated for Wales'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'calibrationEffort',
          label: 'Calibration',
          rows: 3,
          placeholderAttribute: 'If the model is NOT currently calibrated for Wales, how much work would it be to calibrate it?'
        })

      ]
    },
    {
      label: 'Input variables',
      title: 'Input ariables',
      views: [

        new ParentView({
          model: this.model,
          modelAttribute: 'inputs',
          label: 'Inputs',
          ObjectInputView: ErammpModelInputView,
          multiline: true
        })

      ]
    },
    {
      label: 'Output variables',
      title: 'Output ariables',
      views: [

        new ParentView({
          model: this.model,
          modelAttribute: 'outputs',
          label: 'Outputs',
          ObjectInputView: ErammpModelOutputView,
          multiline: true
        })

      ]
    },
    {
      label: 'Other',
      title: 'Other',
      views: [

        new TextareaView({
          model: this.model,
          modelAttribute: 'modelApproach',
          label: 'Modelling approach',
          rows: 5,
          helpText: `
<p>e.g. Statistical, process-based, etc.</p>
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'runtimeTotal',
          label: 'Approximate runtime',
          placeholderAttribute: 'e.g. 10 minutes/45 seconds/several hours'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'runtimeWales',
          label: 'Approximate runtime for Wales',
          placeholderAttribute: 'e.g. 10 minutes/45 seconds/several hours'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'runtimeOptimisation',
          label: 'Optimisation of runtime',
          helpText: `
<p>What work needs to be done to make a model that can be run in seconds rather than minutes? (how would you do it?)</p>
`
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'outputFormats',
          label: 'Output formats',
          placeholderAttribute: 'e.g. NetCDF, dbf, csv, shp'
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'sectors',
          label: 'Sector',
          placeholderAttribute: 'e.g. Agriculture, Forestry'
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'programmingLanguages',
          label: 'Programming languages'
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'operatingSystems',
          label: 'Operating Systems'
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'timeSteps',
          label: 'Time steps',
          placeholderAttribute: 'e.g. daily, annual, decadal',
          helpText: `
<p>Over what time step does the model produce outputs?</p>
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'futureRun',
          label: 'Future distance',
          placeholderAttribute: 'e.g. 2020s, 2100? ',
          helpText: `
<p>How far into the future would you feel comfortable running the model?</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'integrationExperience',
          label: 'Experience of model integration',
          rows: 5,
          helpText: `
<p>How much experience has your team of integrating models?</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'integrationHistory',
          label: 'Integration of this model',
          rows: 5,
          helpText: `
<p>Have you linked this model within an integrated system before?  If so how?</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Keywords',
          ObjectInputView: KeywordView,
          helpText: `
<p>A list of keywords that help to identify and describe the model - used to improve search results and filtering. A keyword may be an entry from a vocabulary (with a uri) or just plain text.</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'onlineResources',
          label: 'Additional Resources',
          ObjectInputView: OnlineLinkView,
          helpText: `
<p>A list of links to additional resources that may be of use to the user. These are in the form of name: url pairs.</p>
`
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
