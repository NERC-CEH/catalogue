define [
  'underscore'
  'jquery'
  'backbone'
  'cs!views/editor/InputView'
  'cs!views/editor/LinkDocumentView'
  'tpl!templates/editor/LinkDocumentSelector.tpl'
], (
  _,
  $,
  Backbone,
  InputView,
  LinkDocumentView,
  template
) -> InputView.extend

  events: ->
    _.extend {}, InputView.prototype.events,
      'keyup #term': -> do @searchOnceComplete
      'change #term': -> do @search
      'change #catalogue': -> do @search
      'click button': -> do @search

  template: template

  initialize: (options) ->
    InputView.prototype.initialize.call @, options

    @searchOnceComplete = _.debounce @search, 500

    @results = new Backbone.Collection()

    @listenTo @results, 'selected', @setSelected
    @listenTo @results, 'reset', @addAll

    do @search

  search: ->
    catalogue = @$('#catalogue').val()
    term = @$('#term').val()

    $.getJSON "/#{catalogue}/documents?term=#{term}", (data) =>
      @results.reset _.chain(data.results).filter((result) -> result.state == 'published').filter((result) -> _.contains result.view, 'public').value()

  addOne: (result) ->
    view = new LinkDocumentView model: result
    @$('#results').append view.render().el

  addAll: ->
    @$('#results').html ''
    @results.each @addOne, @

  setSelected: (identifier) ->
    @model.set @data.modelAttribute, identifier
