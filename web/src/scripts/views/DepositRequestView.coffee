define [
  'backbone'
  'tpl!templates/DatasetOffered.tpl'
  'tpl!templates/PlanningDocuments.tpl'
  'tpl!templates/RelatedDOIs.tpl'
  'tpl!templates/RelatedDOI.tpl'
  'tpl!templates/ScienceDomain.tpl'
], (Backbone, datasetOfferedTpl, planningDocumentsTpl, relatedDOIsTpl, relatedDOITpl, scienceDomainTpl) -> Backbone.View.extend
  initialize: ->
    $('#planningDocs').change => do @planningDocs
    $('#scienceDomain').change => do @scienceDomain
    $('.dataset-remove').click (evt) => @removeDataset evt
    $('#dataset-add').click => do @addDataset

    $('#relatedDatasetsNo').change => do @removeDois
    $('#relatedDatasetsYes').change => do @addDois

    do @addDois if $('input[name="relatedDatasets"]').filter(':checked').val() == 'yes'

  planningDocs: ->
    val = $('#planningDocs').val()
    other = $(planningDocumentsTpl
      active: yes)
    $('.planning-documents').append other if val == 'other'
    do $('#planningDocsOther').remove if val != 'other'

  scienceDomain: ->
    val = $('#scienceDomain').val()
    other = $(scienceDomainTpl
      active: yes)
    $('#science-domain').append other if val == 'other'
    do $('.science-domain-other').remove if val != 'other'
  
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
  
  removeDois: ->
    do $('#related-dois').remove
  
  addDois: ->
    relatedDatasets = $(relatedDOIsTpl
      active: yes)
    $('#relatedDatasets').append relatedDatasets
    do @addDoi
    $('#addDoi').click => do @addDoi

  addDoi: ->
    dois = @model.get('dois') + 1
    @model.set 'dois', dois
    doi = $(relatedDOITpl
      number: dois)
    $('#dois').append doi
    $('.remove-doi').unbind 'click'
    $('.remove-doi').click (evt) => @removeDoi(evt)

  removeDoi: (evt) ->
    $(evt.target).parent().remove()
    do @addDoi if $('.doi').length == 0