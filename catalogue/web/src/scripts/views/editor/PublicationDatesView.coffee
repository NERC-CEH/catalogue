define [
  'jquery'
  'backbone'
  'tpl!templates/editor/PublicationDates.tpl'
  'jquery-ui/datepicker'
], ($, Backbone, template) -> Backbone.View.extend

  events:
    'change #inputPublicationDates': 'updateModel'

  initialize: ->
    do @render

  render: ->
    drd = @model.get 'datasetReferenceDate'
    @$el.html template
      value: drd?.publicationDate

    $('#inputPublicationDates').datepicker
      dateFormat: "yy-mm-dd"
    return @

  updateModel: (event) ->
    drd = @model.get 'datasetReferenceDate'
    target = event.currentTarget
    drd.publicationDate = target.value
    console.log "new publication date: #{drd.publicationDate}"
