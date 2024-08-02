import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  ContactView, DataInfoView,
  FundingView,
  KeywordVocabularyView, NercModelInfoView, OnlineResourceSimpleView,
  AdditionalInfoView,
  ParentView,
  PredefinedParentView, SupplementalView,
  TextareaView
} from '../views'
import { Contact, Funding } from '../models'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'nercModelUse') }

    this.sections = [{
      label: 'General',
      title: 'General',
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
          rows: 6
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Keywords',
          ObjectInputView: KeywordVocabularyView
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'completionDate',
          label: 'Completion date'
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'responsibleParties',
          ModelType: Contact,
          multiline: true,
          label: 'Contacts',
          ObjectInputView: ContactView,
          predefined: {
            BAS: {
              organisationName: 'British Antarctic Survey',
              role: 'pointOfContact',
              email: 'information@bas.ac.uk',
              organisationIdentifier: 'https://ror.org/01rhff309'
            },
            BGS: {
              organisationName: 'British Geological Survey',
              role: 'pointOfContact',
              email: 'enquiries@bgs.ac.uk',
              organisationIdentifier: 'https://ror.org/04a7gbp98'
            },
            CEDA: {
              organisationName: 'Centre for Environmental Data Analysis',
              role: 'pointOfContact',
              organisationIdentifier: 'https://ror.org/04j4kad11'
            },
            NOC: {
              organisationName: 'National Oceanography Centre',
              role: 'pointOfContact',
              organisationIdentifier: 'https://ror.org/00874hx02'
            },
            UKCEH: {
              organisationName: 'UK Centre for Ecology & Hydrology',
              role: 'pointOfContact',
              email: 'enquiries@ceh.ac.uk',
              organisationIdentifier: 'https://ror.org/00pggkr55'
            }
          }
        }),

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
      label: 'Models',
      title: 'Models',
      views: [
        new ParentView({
          model: this.model,
          modelAttribute: 'modelInfos',
          label: 'Model info',
          ObjectInputView: NercModelInfoView,
          multiline: true,
          helpText: `\
<p>Models used in the project</p>
<p>Name - Name of model (searches catalogue for matching models)
<p>Spatial extent of application - What spatial extent best describes the application?</p>
<p>Available spatial data - Can the application be described by either a shapefile/polygon or bounding box coordinates?</p>
<p>Spatial resolution of application - Spatial resolution at which model outputs were generated e.g. 1km²; 5m² (if applicable)</p>
<p>Temporal extent of application (start date) - Start date of application (if applicable)</p>
<p>Temporal extent of application (end date) - End date of application (if applicable)</p>
<p>Temporal resolution of application - Time step used in the model application e.g. 1s; annual (if applicable)</p>
<p>Calibration - How was the model calibrated (if applicable)?</p>\
`
        })
      ]
    },
    {
      label: 'Data',
      title: 'Data',
      views: [
        new ParentView({
          model: this.model,
          modelAttribute: 'inputData',
          label: 'Input Data',
          ObjectInputView: DataInfoView,
          multiline: true,
          helpText: `
<p>Detailed description of input data including: variable name, units, file format, URL to data catalogue record for each input</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'outputData',
          label: 'Output Data',
          ObjectInputView: DataInfoView,
          multiline: true,
          helpText: `
<p>Detailed description of model outputs including: variable name, units, file format, URL to data catalogue record for each output (or alternative location of model outputs from this application)</p>
`
        })
      ]
    },
    {
      label: 'Additional info',
      title: 'Additional info',
      views: [

        new ParentView({
          model: this.model,
          modelAttribute: 'onlineResources',
          label: 'Online resources',
          ObjectInputView: OnlineResourceSimpleView,
          multiline: true,
          listAttribute: `\
<option value='website'/>
<option value='browseGraphic'>Image to display on metadata record</option>\
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'references',
          multiline: true,
          label: 'References',
          ObjectInputView: SupplementalView,
          helpText: `\
<p>You can add information not documented elsewhere here. This includes links to related papers, grey literature or websites.  For example:</p>
<ul><li>papers that cite this resource</li><li>papers/reports that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>
<p>When linking to published articles, please use DOIs whenever possible.</p>
<p><small class='text-danger'><i class='fa-solid fa-exclamation-triangle'> </i> NOTE: Some websites may be maintained for a limited period and may therefore soon become unavailable.</small></p>\
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'additionalInfo',
          multiline: true,
          label: 'Information not recorded elsewhere',
          ObjectInputView: AdditionalInfoView
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
