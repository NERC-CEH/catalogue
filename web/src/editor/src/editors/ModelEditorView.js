import EditorView from '../EditorView'
import InputView from '../InputView'
import SelectView from '../SelectView'
import {
  CheckboxView, KeywordView, LinkView,
  ModelApplicationModelView,
  ParentStringView,
  ParentView, RelationshipView, ResourceIdentifierView, ShortContactView,
  SingleObjectView,
  TextareaView
} from '../views'

export default EditorView.extend({

  initialize () {
    this.sections = [{
      label: 'Common',
      title: 'Common information',
      views: [
        new SelectView({
          model: this.model,
          modelAttribute: 'type',
          label: 'Type',
          options: [
            { value: 'modelApplication', label: 'Model use' },
            { value: 'caseStudy', label: 'Case Study' },
            { value: 'model', label: 'Model' }
          ]
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description',
          rows: 17
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'contact',
          label: 'Contact',
          ObjectInputView: ShortContactView
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'resourceIdentifiers',
          label: 'Resource Identifiers',
          ObjectInputView: ResourceIdentifierView,
          helpText: `\
<p>A unique string or number used to identify the resource.</p>
<p> The codespace identifies the context in which the code is unique.</p>\
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'relationships',
          label: 'Relationships',
          ObjectInputView: RelationshipView,
          multiline: true,
          options: [
            { value: 'http://purl.org/dc/terms/references', label: 'References' }
          ]
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Keywords',
          ObjectInputView: KeywordView
        })

      ]
    },
    {
      label: 'Model',
      title: 'Model only',
      views: [
        new ParentStringView({
          model: this.model,
          modelAttribute: 'keyReferences',
          label: 'Key References'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'version',
          label: 'Version'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'releaseDate',
          label: 'Release Date'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'license',
          label: 'License'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'operatingRequirements',
          label: 'Operating Requirements'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'applicationType',
          label: 'Application Type'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'userInterface',
          label: 'User Interface'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'supportAvailable',
          label: 'Support Available'
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'applicationScale',
          label: 'Application Scale',
          options: [
            { value: 'Plot', label: 'Plot' },
            { value: 'Field', label: 'Field' },
            { value: 'Farm', label: 'Farm' },
            { value: 'River reach', label: 'River reach' },
            { value: 'Catchment', label: 'Catchment' },
            { value: 'Landscape', label: 'Landscape' },
            { value: 'Regional', label: 'Regional' },
            { value: 'National', label: 'National' },
            { value: 'Global', label: 'Global' }
          ]
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'geographicalRestrictions',
          label: 'Geographical Restrictions'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'temporalResolution',
          label: 'Temporal Resolution'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'spatialResolution',
          label: 'Spatial Resolution'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'primaryPurpose',
          label: 'Primary Purpose'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'keyOutputVariables',
          label: 'Key Output Variables'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'keyInputVariables',
          label: 'Key Input Variables'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'calibrationRequired',
          label: 'Calibration Required'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'modelStructure',
          label: 'Model Structure'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'modelParameterisation',
          label: 'Model Parameterisation'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'inputDataAvailableOnCatalogue',
          label: 'Input Data Available on CaMMP Catalogue?'
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'inputData',
          label: 'Input Data'
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'outputData',
          label: 'Output Data'
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'documentation',
          label: 'Documentation',
          ObjectInputView: LinkView
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'developerTesting',
          label: 'Developer Testing'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'internalPeerReview',
          label: 'Internal Peer Review'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'externalPeerReview',
          label: 'External Peer Review'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'useOfVersionControl',
          label: 'Use of Version Control'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'internalModelAudit',
          label: 'Internal Model Audit'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'externalModelAudit',
          label: 'External Model Audit'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'qualityAssuranceGuidelinesAndChecklists',
          label: 'Quality Assurance Guidelines and Checklists'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'governance',
          label: 'Governance'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'transparency',
          label: 'Transparency'
        }),

        new CheckboxView({
          model: this.model,
          modelAttribute: 'periodicReview',
          label: 'Periodic Review'
        })

      ]
    },
    {
      label: 'Model use',
      title: 'Model use only',
      views: [
        new InputView({
          model: this.model,
          modelAttribute: 'relevanceToCaMMP',
          label: 'Relevance to CaMMP'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'date',
          label: 'Date'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'studySite',
          label: 'Study Site'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'studyScale',
          label: 'Study Scale'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'objective',
          label: 'Objective'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'funderDetails',
          label: 'Funder Details'
        }),

        new ParentStringView({
          model: this.model,
          modelAttribute: 'inputData',
          label: 'Input Data'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'multipleModelsUsed',
          label: 'Multiple Models Used'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'multipleModelLinkages',
          label: 'Multiple Model Linkages'
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'models',
          label: 'Models Used',
          ObjectInputView: ModelApplicationModelView,
          multiline: true
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'sensitivity',
          label: 'Sensitivity',
          options: [
            { value: 'strongly disagree', label: 'strongly disagree' },
            { value: 'disagree', label: 'disagree' },
            { value: 'neither agree nor disagree', label: 'neither agree nor disagree' },
            { value: 'agree', label: 'agree' },
            { value: 'strongly agree', label: 'strongly agree' }
          ]
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'uncertainty',
          label: 'Uncertainty',
          options: [
            { value: 'strongly disagree', label: 'strongly disagree' },
            { value: 'disagree', label: 'disagree' },
            { value: 'neither agree nor disagree', label: 'neither agree nor disagree' },
            { value: 'agree', label: 'agree' },
            { value: 'strongly agree', label: 'strongly agree' }
          ]
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'validation',
          label: 'Validation',
          options: [
            { value: 'strongly disagree', label: 'strongly disagree' },
            { value: 'disagree', label: 'disagree' },
            { value: 'neither agree nor disagree', label: 'neither agree nor disagree' },
            { value: 'agree', label: 'agree' },
            { value: 'strongly agree', label: 'strongly agree' }
          ]
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'modelEasyToUse',
          label: 'Model Easy to Use',
          options: [
            { value: 'strongly disagree', label: 'strongly disagree' },
            { value: 'disagree', label: 'disagree' },
            { value: 'neither agree nor disagree', label: 'neither agree nor disagree' },
            { value: 'agree', label: 'agree' },
            { value: 'strongly agree', label: 'strongly agree' }
          ]
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'userManualUseful',
          label: 'User Manual Useful',
          options: [
            { value: 'strongly disagree', label: 'strongly disagree' },
            { value: 'disagree', label: 'disagree' },
            { value: 'neither agree nor disagree', label: 'neither agree nor disagree' },
            { value: 'agree', label: 'agree' },
            { value: 'strongly agree', label: 'strongly agree' }
          ]
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'dataObtainable',
          label: 'Data Obtainable',
          options: [
            { value: 'strongly disagree', label: 'strongly disagree' },
            { value: 'disagree', label: 'disagree' },
            { value: 'neither agree nor disagree', label: 'neither agree nor disagree' },
            { value: 'agree', label: 'agree' },
            { value: 'strongly agree', label: 'strongly agree' }
          ]
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'modelUnderstandable',
          label: 'Model Understandable',
          options: [
            { value: 'strongly disagree', label: 'strongly disagree' },
            { value: 'disagree', label: 'disagree' },
            { value: 'neither agree nor disagree', label: 'neither agree nor disagree' },
            { value: 'agree', label: 'agree' },
            { value: 'strongly agree', label: 'strongly agree' }
          ]
        })
      ]
    },
    {
      label: 'Case Study',
      title: 'Case Study only',
      views: [
        new SingleObjectView({
          model: this.model,
          modelAttribute: 'caseStudy',
          label: 'Case Study Link',
          ObjectInputView: LinkView
        })

      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
