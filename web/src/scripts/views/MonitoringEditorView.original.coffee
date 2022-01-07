define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
], (EditorView, SingleObjectView, InputView, TextareaView) -> EditorView.extend

  initialize: ->
    @sections = [
      label: 'One'
      views: [
        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Title help</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 17
          helpText: """
                    <p>Description help</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @