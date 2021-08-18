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

  initialize: ->
    ObjectInputView.prototype.initialize.apply @
    @vocabularies = new Backbone.Collection()
    catalogue = $('html').data('catalogue')
    @$vocabularies = @$('.vocabularies')
    @listenTo(@vocabularies, 'add', @addOne)
    @listenTo(@vocabularies, 'reset', @addAll)
    $.getJSON "/catalogues/#{catalogue}", (data) =>
      @vocabularies.reset(data.vocabularies)

    @$('.autocomplete').autocomplete
      minLength: 2
      source: (request, response) =>
        vocab = _.pluck(@vocabularies.where({'toSearch': true}), 'id')
        term = request.term.trim()
        if _.isEmpty term
          query = "/vocabulary/keywords?vocab=#{vocab}"
        else
          query = "/vocabulary/keywords?query=#{request.term}?vocab=#{vocab}"
        $.getJSON query, (data) ->
            response _.map data, (d) -> {value: d.label, label: d.label, id: d.vocabId, url: d.url}

    @$('.autocomplete').on 'autocompleteselect', (event, ui) =>
        @model.set 'vocabId', ui.item.id
        @$('.vocabId').val ui.item.id
        @model.set 'label', ui.item.label
        @$('.label').val ui.item.label
        @model.set 'url', ui.item.url
        @$('.url').val ui.item.url


  addAll: ->
    @vocabularies.each(@addOne, @)

  addOne: (vocabulary) ->
    vocabulary.set('toSearch': true)
    view = new KeywordCheckboxView({model: vocabulary})
    @$vocabularies.append(view.render().el)

  searchChecked: ->
    @vocabularies.where({'toSearch': true})
