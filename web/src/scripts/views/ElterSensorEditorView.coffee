define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
], (
  EditorView
  InputView
) -> EditorView.extend

  initialize: ->
    @model.set('type', 'dataset') unless @model.has('type')

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [
          new InputView
            model: @model
            modelAttribute: 'title'
            label: 'Title'
      ]
    ]

    EditorView.prototype.initialize.apply @