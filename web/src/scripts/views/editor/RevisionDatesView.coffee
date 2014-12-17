define [
  'jquery'
  'backbone'
  'tpl!templates/editor/RevisionDates.tpl'
  'jquery-ui/datepicker'
], ($, Backbone, template) -> Backbone.View.extend

  events:
    'change #inputRevisionDates': 'updateModel'

  initialize: ->
    do @render

  render: ->
    drd = @model.get 'datasetReferenceDate'
    @$el.html template
      value: drd.revisionDate

    $('#inputRevisionDates').datepicker
      dateFormat: "yy-mm-dd"
    return @

  updateModel: (event) ->
    drd = @model.get 'datasetReferenceDate'
    target = event.currentTarget
    drd.revisionDate = target.value
    console.log "new revision date: #{drd.revisionDate}"
