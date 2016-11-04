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
  ], (EditorView, InputView, TextareaView, ParentStringView, ParentView, LinkView, ResourceIdentifierView, SelectView, RelationshipView) -> EditorView.extend

  initialize: ->
    @sections = [
      label: 'One'
      title:  'Common information'
      views: [

        new ParentView
          model: @model
          modelAttribute: 'relationships'
          label: 'Relationships'
          ObjectInputView: RelationshipView

        new SelectView
          model: @model
          modelAttribute: 'type'
          label: 'Type'
          options: [
            {value: 'modelApplication', label: 'Application of Model'},
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

        new ParentView
          model: @model
          modelAttribute: 'resourceIdentifiers'
          label: 'Resource Identifiers'
          ObjectInputView: ResourceIdentifierView
          helpText: """
                    <p>A unique string or number used to identify the resource.</p>
                    <p> The codespace identifies the context in which the code is unique.</p>
                    """
      ]
    ,
      label: 'Two'
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
          modelAttribute: 'contact'
          label: 'Contact'

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
          modelAttribute: 'smallestAndLargestApplication'
          label: 'Smallest And Largest Application'

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
          modelAttribute: 'keyOutputs'
          label: 'Key Outputs'

        new InputView
          model: @model
          modelAttribute: 'calibrationRequired'
          label: 'Calibration Required'

        new InputView
          model: @model
          modelAttribute: 'dataInput'
          label: 'Data Input'

        new ParentStringView
          model: @model
          modelAttribute: 'inputData'
          label: 'Input Data'

        new ParentStringView
          model: @model
          modelAttribute: 'outputData'
          label: 'Output Data'

        new ParentView
          model: @model
          modelAttribute: 'documentation'
          label: 'Documentation'
          ObjectInputView: LinkView

        new ParentView
          model: @model
          modelAttribute: 'modelApplications'
          label: 'Model Applications'
          ObjectInputView: LinkView

        new ParentView
          model: @model
          modelAttribute: 'links'
          label: 'Links'
          ObjectInputView: LinkView

        ]
      ,
      label: 'Three'
      title: 'Application of model only'
      views: [
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
      ]
    ]

    EditorView.prototype.initialize.apply @