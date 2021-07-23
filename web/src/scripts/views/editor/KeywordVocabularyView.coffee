define [
  'underscore'
  'jquery'
  'backbone'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/KeywordVocabulary.tpl'
  'cs!views/editor/KeywordCheckboxView'
  'jquery-ui/autocomplete'
], (_, $, Backbone, ObjectInputView, template, KeywordCheckboxView) -> ObjectInputView.extend
  template: template
  events:
    'click .search-selected': 'searchSelected'
    'click .select-all': 'selectAll'
  initialize: ->
    ObjectInputView.prototype.initialize.apply @
    @checkboxes = new Backbone.Collection()
    @$vocabulariesView = @$('.vocabularies')
    $.getJSON "/catalogues/#{catalogues}", (data) =>
          @checkboxes.reset(data.vocabularies)
    @listenTo(@checkboxes, 'add', @addOne)
    @listenTo(@checkboxes, 'reset', @addAll)

  vocabIds = []
  addAll: ->
    @checkboxes.each(@addOne, @)

  addOne: (vocabulary) ->
    view = new KeywordCheckboxView({model: vocabulary.id})
    @$vocabulariesViews.append(view.render().el)

  toSearch: ->
    vocabIds.addAll(@checkboxes.where({'toDelete': true}))

  selectAll: ->
   @vocabulary.invoke('set', 'toSearch', true)

  @$('.autocomplete').autocomplete
    minLength: 2
    source: (request, response) ->
      term = request.term.trim()
      if _.isEmpty term
        query = string.concat("/vocabulary/keywords?vocab=#{}")
      else
        query = string.concat("/vocabulary/keywords?query=#{request.term}?vocab=#{}")
      $.getJSON query, (data) ->
        response _.map data, (d) -> {value: d.label, label: d.label, id: d.vocabId, url: d.url}

  @$('.autocomplete').on 'autocompleteselect', (event, ui) =>
      @model.set 'vocabId', ui.item.id
      @$('.vocabId').val ui.item.id
      @model.set 'label', ui.item.label
      @$('.label').val ui.item.label
      @model.set 'url', ui.item.url
      @$('.url').val ui.item.url