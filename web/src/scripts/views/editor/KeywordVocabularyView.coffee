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
    @$vocabularies = @$('.vocabularyPicker')
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
          query = "/vocabulary/keywords?query=#{request.term}&vocab=#{vocab}"
        $.getJSON query, (data) ->
            response _.map data, (d) -> {value: d.label, label: "#{d.label} (#{d.vocabId})", url: d.url}

    @$('.autocomplete').on 'autocompleteselect', (event, ui) =>
      @model.set 'value', ui.item.label
      @$('.value').val ui.item.label
      @model.set 'uri', ui.item.url
      @$('.uri').val ui.item.url


  addAll: ->
    @vocabularies.each(@addOne, @)

  addOne: (vocabulary) ->
    vocabulary.set('toSearch': true)
    view = new KeywordCheckboxView({model: vocabulary})
    @$vocabularies.append(view.render().el)
