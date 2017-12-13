define [
  'backbone'
  'tpl!templates/DatasetOffered.tpl'
  'tpl!templates/PlanningDocuments.tpl'
], (Backbone, datasetOfferedTpl, planningDocumentsTpl) -> Backbone.View.extend
  initialize: ->
    $('#planningDocs').change ->
      val = $('#planningDocs').val()
      other = $(planningDocumentsTpl
        active: yes)
      $('.planning-documents').append other if val == 'other'
      do $('#planningDocsOther').remove if val != 'other'

    $('.dataset-remove').click (evt) => @removeDataset(evt)
    $('#dataset-add').click => do @addDataset

  removeDataset: (evt) ->
    $(evt.target).parent().remove()
    do @addDataset if $('.dataset').length == 0

  addDataset: ->
    datasets = @model.get('datasets') + 1
    @model.set 'datasets', datasets
    dataset = $(datasetOfferedTpl
      number: datasets)
    $('.datasets-offered').append dataset

    $('.dataset-remove').unbind 'click'
    $('.dataset-remove').click (evt) => @removeDataset(evt)