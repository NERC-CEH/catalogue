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
    @vocabularies = new Backbone.Collection()
    catalogue = $('html').data('catalogue')
    @$vocabularies = @$('.vocabularies')
    @listenTo(@vocabularies, 'add', @addOne)
    @listenTo(@vocabularies, 'reset', @addAll)
    $.getJSON "/catalogues/#{catalogue}", (data) =>
      @vocabularies.reset(data.vocabularies)


  vocabIds = []

  addAll: ->
    console.log("add all")
    @vocabularies.each(@addOne, @)

  addOne: (vocabulary) ->
    console.log("add one")
    view = new KeywordCheckboxView({model: vocabulary})
    @$vocabularies.append(view.render().el)

  toSearch: ->
    console.log("to search")
    vocabIds.addAll(@vocabularies.where({'toSearch': true}))

  selectAll: ->
   console.log("select all")
   @vocabularies.invoke('set', 'toSearch', true)

  @$('.autocomplete').autocomplete
    console.log("autocomplete")
    minLength: 2
    source: (request, response) ->
      term = request.term.trim()
      if _.isEmpty term
        query = "/vocabulary/keywords?vocab=#{vocabIds}"
      else
        query = "/vocabulary/keywords?query=#{request.term}?vocab=#{vocabIds}"
      $.getJSON query, (data) ->
        response _.map data, (d) -> {value: d.label, label: d.label, id: d.vocabId, url: d.url}

  @$('.autocomplete').on 'autocompleteselect', (event, ui) =>
      console.log("autocompleteselect")
      @model.set 'vocabId', ui.item.id
      @$('.vocabId').val ui.item.id
      @model.set 'label', ui.item.label
      @$('.label').val ui.item.label
      @model.set 'url', ui.item.url
      @$('.url').val ui.item.url