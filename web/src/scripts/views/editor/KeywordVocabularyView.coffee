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
    @toSearch = new Backbone.Collection()
    @vocabularies = new Backbone.Collection()
    catalogue = $('html').data('catalogue')
    @$vocabularies = @$('.vocabularies')
    @listenTo(@vocabularies, 'add', @addOne)
    @listenTo(@vocabularies, 'reset', @addAll)
    $.getJSON "/catalogues/#{catalogue}", (data) =>
      @vocabularies.reset(data.vocabularies)


  addAll: ->
    @vocabularies.each(@addOne, @)

  addOne: (vocabulary) ->
    vocabulary.set('toSearch': true)
    view = new KeywordCheckboxView({model: vocabulary})
    @$vocabularies.append(view.render().el)

  searchSelected: ->
    @toSearch =  @vocabularies.where({'toSearch': true})
    console.log("searchSelected")
    console.log(@toSearch)

  selectAll: ->
   console.log("select all")
   @vocabularies.invoke('set', 'toSearch', true)

  @$('.autocomplete').autocomplete
    console.log("autocomplete")
    minLength: 2
    source: (request, response) ->
      vocab = []
      vocab =  _.pluck(@toSearch.models, 'id')
      console.log(vocab)
      term = request.term.trim()
      if _.isEmpty term
        console.log(@toSearch)
        query = "/vocabulary/keywords?vocab=#{vocab}"
      else
        console.log(@toSearch)
        query = "/vocabulary/keywords?query=#{request.term}?vocab=#{vocab}"
      console.log("reached")
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