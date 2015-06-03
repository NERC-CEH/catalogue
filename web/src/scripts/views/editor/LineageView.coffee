define [
  'cs!views/editor/SingleStringView'
  'cs!views/editor/TextareaView'
], (SingleStringView, TextareaView) -> SingleStringView.extend
  modelAttribute: 'lineage'
  label: 'Lineage'
  helpText: """
            Information about the source data used in the construction of this data resource. Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.
            """

  initialize: ->
    do @render
    new TextareaView
      el: @$('.dataentry')
      model: @model
      modelAttribute: @modelAttribute
      rows: 15