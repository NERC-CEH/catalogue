define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/ParentView'
  'cs!views/editor/LinkView'
  ], (EditorView, InputView) -> EditorView.extend

  initialize: ->
    @sections = [
      label: 'One'
      title:  'General information'
      views: [
        new InputView
          model: @model
          modelAttribute: 'linkedDocumentId'
          label: 'Identifier of linked Document'
        ]
    ]

    EditorView.prototype.initialize.apply @