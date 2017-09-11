define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/ParentView'
  'cs!views/editor/LinkView'
  'cs!views/editor/ResourceIdentifierView'
  'cs!views/editor/SelectView'
  'cs!views/editor/RelationshipView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/CheckboxView'
  'cs!views/editor/ShortContactView'
  'cs!views/editor/ModelApplicationModelView'
], (EditorView,
    InputView,
    TextareaView,
    ParentStringView,
    ParentView,
    LinkView,
    ResourceIdentifierView,
    SelectView,
    RelationshipView,
    SingleObjectView,
    KeywordView,
    CheckboxView,
    ShortContactView,
    ModelApplicationModelView) -> EditorView.extend

  initialize: ->
    @sections = [
      label: 'Common'
      title:  'Common information'
      views: [
        new SelectView
          model: @model
          modelAttribute: 'type'
          label: 'Type'
          options: [
            {value: 'modelApplication', label: 'Model Application'},
            {value: 'caseStudy', label: 'Case Study'},
            {value: 'model', label: 'Model'}
          ]

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 17

        new SingleObjectView
          model: @model
          modelAttribute: 'contact'
          label: 'Contact'
          ObjectInputView: ShortContactView

        new ParentView
          model: @model
          modelAttribute: 'resourceIdentifiers'
          label: 'Resource Identifiers'
          ObjectInputView: ResourceIdentifierView
          helpText: """
                    <p>A unique string or number used to identify the resource.</p>
                    <p> The codespace identifies the context in which the code is unique.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'relationships'
          label: 'Relationships'
          ObjectInputView: RelationshipView
          multiline: true
          options: [
            {value: 'http://purl.org/dc/terms/references', label: 'References'}
          ]

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView

      ]
    ,
      label: 'Model'
      title: 'Model only'
      views: [
        new ParentStringView
          model: @model
          modelAttribute: 'keyReferences'
          label: 'Key References'

        new InputView
          model: @model
          modelAttribute: 'version'
          label: 'Version'

        new InputView
          model: @model
          modelAttribute: 'releaseDate'
          label: 'Release Date'

        new InputView
          model: @model
          modelAttribute: 'license'
          label: 'License'

        new InputView
          model: @model
          modelAttribute: 'operatingRequirements'
          label: 'Operating Requirements'

        new InputView
          model: @model
          modelAttribute: 'applicationType'
          label: 'Application Type'

        new InputView
          model: @model
          modelAttribute: 'userInterface'
          label: 'User Interface'

        new InputView
          model: @model
          modelAttribute: 'supportAvailable'
          label: 'Support Available'

        new SelectView
          model: @model
          modelAttribute: 'applicationScale'
          label: 'Application Scale'
          options: [
            {value: 'Plot', label: 'Plot'},
            {value: 'Field', label: 'Field'},
            {value: 'Farm', label: 'Farm'},
            {value: 'River reach', label: 'River reach'},
            {value: 'Catchment', label: 'Catchment'},
            {value: 'Landscape', label: 'Landscape'},
            {value: 'Regional', label: 'Regional'},
            {value: 'National', label: 'National'},
            {value: 'Global', label: 'Global'}
          ]

        new InputView
          model: @model
          modelAttribute: 'geographicalRestrictions'
          label: 'Geographical Restrictions'

        new InputView
          model: @model
          modelAttribute: 'temporalResolution'
          label: 'Temporal Resolution'

        new InputView
          model: @model
          modelAttribute: 'spatialResolution'
          label: 'Spatial Resolution'

        new InputView
          model: @model
          modelAttribute: 'primaryPurpose'
          label: 'Primary Purpose'

        new InputView
          model: @model
          modelAttribute: 'keyOutputVariables'
          label: 'Key Output Variables'

        new InputView
          model: @model
          modelAttribute: 'keyInputVariables'
          label: 'Key Input Variables'

        new InputView
          model: @model
          modelAttribute: 'calibrationRequired'
          label: 'Calibration Required'

        new InputView
          model: @model
          modelAttribute: 'modelStructure'
          label: 'Model Structure'

        new InputView
          model: @model
          modelAttribute: 'modelParameterisation'
          label: 'Model Parameterisation'

        new InputView
          model: @model
          modelAttribute: 'inputDataAvailableOnCatalogue'
          label: 'Input Data Available on CaMMP Catalogue?'

        new ParentStringView
          model: @model
          modelAttribute: 'inputData'
          label: 'Input Data'

        new ParentStringView
          model: @model
          modelAttribute: 'outputData'
          label: 'Output Data'

        new SingleObjectView
          model: @model
          modelAttribute: 'documentation'
          label: 'Documentation'
          ObjectInputView: LinkView

        new CheckboxView
          model: @model
          modelAttribute: 'developerTesting'
          label: 'Developer Testing'

        new CheckboxView
          model: @model
          modelAttribute: 'internalPeerReview'
          label: 'Internal Peer Review'

        new CheckboxView
          model: @model
          modelAttribute: 'externalPeerReview'
          label: 'External Peer Review'

        new CheckboxView
          model: @model
          modelAttribute: 'useOfVersionControl'
          label: 'Use of Version Control'

        new CheckboxView
          model: @model
          modelAttribute: 'internalModelAudit'
          label: 'Internal Model Audit'

        new CheckboxView
          model: @model
          modelAttribute: 'externalModelAudit'
          label: 'External Model Audit'

        new CheckboxView
          model: @model
          modelAttribute: 'qualityAssuranceGuidelinesAndChecklists'
          label: 'Quality Assurance Guidelines and Checklists'

        new CheckboxView
          model: @model
          modelAttribute: 'governance'
          label: 'Governance'

        new CheckboxView
          model: @model
          modelAttribute: 'transparency'
          label: 'Transparency'

        new CheckboxView
          model: @model
          modelAttribute: 'periodicReview'
          label: 'Periodic Review'

        ]
      ,
        label: 'Model Application'
        title: 'Model application only'
        views: [
          new InputView
            model: @model
            modelAttribute: 'relevanceToCaMMP'
            label: 'Relevance to CaMMP'

          new InputView
            model: @model
            modelAttribute: 'date'
            label: 'Date'

          new InputView
            model: @model
            modelAttribute: 'studySite'
            label: 'Study Site'

          new InputView
            model: @model
            modelAttribute: 'studyScale'
            label: 'Study Scale'

          new InputView
            model: @model
            modelAttribute: 'objective'
            label: 'Objective'

          new TextareaView
            model: @model
            modelAttribute: 'funderDetails'
            label: 'Funder Details'

          new ParentStringView
            model: @model
            modelAttribute: 'inputData'
            label: 'Input Data'

          new InputView
            model: @model
            modelAttribute: 'multipleModelsUsed'
            label: 'Multiple Models Used'

          new InputView
            model: @model
            modelAttribute: 'multipleModelLinkages'
            label: 'Multiple Model Linkages'

          new ParentView
            model: @model
            modelAttribute: 'models'
            label: 'Models Used'
            ObjectInputView: ModelApplicationModelView
            multiline: true

          new SelectView
            model: @model
            modelAttribute: 'sensitivity'
            label: 'Sensitivity'
            options: [
              {value: 'strongly disagree', label: 'strongly disagree'},
              {value: 'disagree', label: 'disagree'},
              {value: 'neither agree nor disagree', label: 'neither agree nor disagree'},
              {value: 'agree', label: 'agree'},
              {value: 'strongly agree', label: 'strongly agree'}
            ]

          new SelectView
            model: @model
            modelAttribute: 'uncertainty'
            label: 'Uncertainty'
            options: [
              {value: 'strongly disagree', label: 'strongly disagree'},
              {value: 'disagree', label: 'disagree'},
              {value: 'neither agree nor disagree', label: 'neither agree nor disagree'},
              {value: 'agree', label: 'agree'},
              {value: 'strongly agree', label: 'strongly agree'}
            ]
          
          new SelectView
            model: @model
            modelAttribute: 'validation'
            label: 'Validation'
            options: [
              {value: 'strongly disagree', label: 'strongly disagree'},
              {value: 'disagree', label: 'disagree'},
              {value: 'neither agree nor disagree', label: 'neither agree nor disagree'},
              {value: 'agree', label: 'agree'},
              {value: 'strongly agree', label: 'strongly agree'}
            ]

          new SelectView
            model: @model
            modelAttribute: 'modelEasyToUse'
            label: 'Model Easy to Use'
            options: [
              {value: 'strongly disagree', label: 'strongly disagree'},
              {value: 'disagree', label: 'disagree'},
              {value: 'neither agree nor disagree', label: 'neither agree nor disagree'},
              {value: 'agree', label: 'agree'},
              {value: 'strongly agree', label: 'strongly agree'}
            ]

          new SelectView
            model: @model
            modelAttribute: 'userManualUseful'
            label: 'User Manual Useful'
            options: [
              {value: 'strongly disagree', label: 'strongly disagree'},
              {value: 'disagree', label: 'disagree'},
              {value: 'neither agree nor disagree', label: 'neither agree nor disagree'},
              {value: 'agree', label: 'agree'},
              {value: 'strongly agree', label: 'strongly agree'}
            ]

          new SelectView
            model: @model
            modelAttribute: 'dataObtainable'
            label: 'Data Obtainable'
            options: [
              {value: 'strongly disagree', label: 'strongly disagree'},
              {value: 'disagree', label: 'disagree'},
              {value: 'neither agree nor disagree', label: 'neither agree nor disagree'},
              {value: 'agree', label: 'agree'},
              {value: 'strongly agree', label: 'strongly agree'}
            ]

          new SelectView
            model: @model
            modelAttribute: 'modelUnderstandable'
            label: 'Model Understandable'
            options: [
              {value: 'strongly disagree', label: 'strongly disagree'},
              {value: 'disagree', label: 'disagree'},
              {value: 'neither agree nor disagree', label: 'neither agree nor disagree'},
              {value: 'agree', label: 'agree'},
              {value: 'strongly agree', label: 'strongly agree'}
            ]
        ]
      ,
        label: 'Case Study'
        title: 'Case Study only'
        views: [
          new SingleObjectView
            model: @model
            modelAttribute: 'caseStudy'
            label: 'Case Study Link'
            ObjectInputView: LinkView

        ]
    ]

    EditorView.prototype.initialize.apply @