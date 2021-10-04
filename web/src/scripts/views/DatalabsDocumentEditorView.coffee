define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentStringView'
], (
  EditorView
  InputView
  TextareaView
  ParentStringView
) -> EditorView.extend

  initialize: ->
    @model.set 'type', 'dataset' unless @model.has 'type'

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [
          new InputView
            model: @model
            modelAttribute: 'title'
            label: 'Title'
          new InputView
            model: @model
            modelAttribute: 'description'
            label: 'Description'
          new InputView
            model: @model
            modelAttribute: 'version'
            label: 'Version'
          new InputView
            model: @model
            modelAttribute: 'masterUrl'
            label: 'Master URL'
          new ParentStringView
            model: @model
            modelAttribute: 'owners'
            label: 'Owners'
      ]
    ]

    EditorView.prototype.initialize.apply @