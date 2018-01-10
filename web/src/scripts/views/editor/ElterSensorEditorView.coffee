define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/SelectView'
  'cs!views/editor/KeywordValueInputView'
  'cs!views/editor/ParentView'
  'cs!views/editor/ManufacturerView'
], (
  EditorView
  InputView
  TextareaView
  SelectView
  KeywordValueInputView
  ParentView
  ManufacturerView
) -> EditorView.extend

  initialize: ->
    @model.set('type', 'collectionHardware') unless @model.has('type')

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [
          new InputView
            model: @model
            modelAttribute: 'title'
            label: 'Long Name'
          new InputView
            model: @model
            modelAttribute: 'shortName'
            label: 'Short Name'
          new TextareaView
            model: @model
            modelAttribute: 'description'
            label: 'Description'
          new InputView
            model: @model
            modelAttribute: 'serialNumber'
            label: 'Serial Number'
          new InputView
            model: @model
            modelAttribute: 'documentation'
            label: 'Documentation'
          new ManufacturerView
            model: @model
            modelAttribute: 'manufacturer'
            label: 'Manufacturer'
          new SelectView
            model: @model
            modelAttribute: 'processType'
            label: 'Process Type'
            options: [
              { value: 'Simulation', label: 'Simulation' }
              { value: 'Manual', label: 'Manual' }
              { value: 'Sensor', label: 'Sensor' }
              { value: 'Algorithm', label: 'Algorithm' }
              { value: 'Unknown', label: 'Unknown' }
            ]
          new ParentView
            model: @model
            modelAttribute: 'defaultParameters'
            label: 'Default Parameters'
            ObjectInputView: KeywordValueInputView
      ]
    ]

    EditorView.prototype.initialize.apply @