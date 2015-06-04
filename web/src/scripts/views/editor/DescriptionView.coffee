define [
  'cs!views/editor/SingleView'
  'cs!views/editor/TextareaView'
], (SingleView, TextareaView) -> SingleView.extend
  modelAttribute: 'description'
  label: 'Description'
  helpText: """
            A brief description of the data resource. This should include some explanation as to purpose and how the data resource has been used since creation. It is best to write a concise abstract.
            """

  initialize: ->
    do @render
    new TextareaView
      el: @$('.dataentry')
      model: @model
      modelAttribute: @modelAttribute
      rows: 15