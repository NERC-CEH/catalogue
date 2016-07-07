define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/ParentView'
  'cs!views/editor/LinkView'
  ], (EditorView, SingleObjectView, InputView, TextareaView, ParentStringView, ParentView, LinkView) -> EditorView.extend

  initialize: ->
    console.log 'hello'
    @sections = [
      label: 'One'
      title:  'General information'
      views: [
        new SingleObjectView
          model: @model
          modelAttribute: 'type'
          label: 'Type'
          ObjectInputView: InputView

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
              
        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 17
           
        new ParentStringView
          model: @model
          modelAttribute: 'keyReferences'
          label: 'Key References'
        
        new InputView
          model: @model
          modelAttribute: 'id'
          label: 'File Identifier'
          readonly: true

        new InputView
          model: @model
          modelAttribute: 'uri'
          label: 'URL'
          readonly: true
          
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
    ]
    
    console.log @sections
    
    EditorView.prototype.initialize.apply @