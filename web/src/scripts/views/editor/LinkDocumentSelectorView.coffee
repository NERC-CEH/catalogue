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

  optionTemplate: _.template "<option value=\"<%= id %>\" <% if (id === data.catalogue) { %>selected<% } %>><%= title %></option>"

  initialize: (options) ->
    if @model.isNew()
      @currentCatalogue = Backbone.history.location.pathname.split('/')[1]
    else
      @currentCatalogue = @model.get('metadata').catalogue

    options.catalogue = 'eidc'

    @searchOnceComplete = _.debounce @search, 500
    @results = new Backbone.Collection()

    $.getJSON '/catalogues', (catalogues) =>
      @catalogues = catalogues
      InputView.prototype.initialize.call @, options

    @listenTo @results, 'selected', @setSelected
    @listenTo @results, 'reset', @addAll

  render: ->
    InputView.prototype.render.apply @
    $select = @$ '#catalogue'
    _.chain(@catalogues).reject((c) => c.id == @currentCatalogue ).each (catalogue) =>
      $select.append @optionTemplate _.extend {}, catalogue, data: @data
    do @search
    @

  search: ->
    @data.catalogue = @$('#catalogue').val()
    @data.term = @$('#term').val()

    if @data.term.length > 0
      searchUrl = "/#{@data.catalogue}/documents?term=state:published AND view:public AND NOT documentType:LINK_DOCUMENT AND #{@data.term}"
    else
      searchUrl = "/#{@data.catalogue}/documents?term=state:published AND view:public AND NOT documentType:LINK_DOCUMENT"

    $.getJSON searchUrl, (data) =>
      @results.reset data.results

  addOne: (result) ->
    view = new LinkDocumentView model: result
    @$('#results').append view.render().el

  addAll: ->
    @$('#results').html ''
    @results.each @addOne, @

  setSelected: (identifier) ->
    @model.set @data.modelAttribute, identifier
