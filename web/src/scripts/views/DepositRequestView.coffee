define [
  'backbone'
  'tpl!templates/DatasetOffered.tpl'
], (Backbone, datasetOfferedTpl) -> Backbone.View.extend
  initialize: ->
    $('#planningDocs').change ->
      val = $('#planningDocs').val()
      $('#planningDocsOther').removeClass 'is-active'
      $('#planningDocsOther').removeClass 'is-inactive'
      $('#planningDocsOther').addClass 'is-active' if val == 'other'
      $('#planningDocsOther').addClass 'is-inactive' if val != 'other'

    $('.dataset-remove').click (evt) ->
      $(evt.target).parent().remove()

    $('#dataset-add').click =>
      datasets = @model.get('datasets') + 1
      @model.set 'datasets', datasets
      dataset = $(datasetOfferedTpl
        number: datasets)
      $('.datasets-offered').append dataset

      $('.dataset-remove').unbind 'click'
      $('.dataset-remove').click (evt) ->
        $(evt.target).parent().remove()
