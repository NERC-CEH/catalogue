define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/SelectView'
  'cs!views/editor/KeywordValueInputView'
  'cs!views/editor/ParentView'
], (
  EditorView
  InputView
  TextareaView
  SelectView
  KeywordValueInputView
  ParentView
) -> EditorView.extend

  initialize: ->
    @model.set('type', 'agent')

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [
          new InputView
            model: @model
            modelAttribute: 'title'
            label: 'Trade Name'
          new InputView
            model: @model
            modelAttribute: 'website'
            label: 'Website'
      ]
    ]

    EditorView.prototype.initialize.apply @