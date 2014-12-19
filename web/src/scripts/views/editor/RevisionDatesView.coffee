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
      value: drd?.revisionDate

    $('#inputRevisionDates').datepicker
      dateFormat: "yy-mm-dd"
    return @

  updateModel: (event) ->
    target = event.currentTarget
    drd = @model.get 'datasetReferenceDate'

    if not drd?
      @model.set 'datasetReferenceDate',
        revisionDate: target.value
    else
      drd.revisionDate = target.value

    console.log "new revision date: #{drd?.revisionDate}"
