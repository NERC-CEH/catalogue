define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!models/editor/String'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
], (EditorView, SingleObjectView, String, InputView, TextareaView) -> EditorView.extend

  initialize: ->
    @sections = [
      label: 'One'
      views: [
        new SingleObjectView
          model: @model
          modelAttribute: 'title'
          ModelType: String
          label: 'Title'
          ObjectInputView: InputView,
          helpText: """
                    <p>Title help</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'description'
          ModelType: String
          label: 'Description'
          ObjectInputView: TextareaView,
          rows: 17
          helpText: """
                    <p>Description help</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @