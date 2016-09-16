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

  catalogues: [
    {'value': 'assist', 'label': 'Achieving Sustainable Agricultural Systems'}
    {'value': 'ceh', 'label': 'Centre for Ecology &amp; Hydrology'}
    {'value': 'cmp', 'label': 'Catchment Management Platform'}
    {'value': 'eidc', 'label': 'Environmental Information Data Centre'}
  ]

  initialize: (options) ->
    InputView.prototype.initialize.call @, options

    currentCatalogue = window.location.pathname.split('/')[1]

    optionTemplate = _.template "<option value=\"<%= value %>\" <% if(value==='eidc') { %>selected<% } %>><%= label %></option>"

    $select = @$ '#catalogue'

    _.chain(@catalogues).reject((c) -> c.value == currentCatalogue ).each (catalogue) ->
      $select.append optionTemplate catalogue

    @searchOnceComplete = _.debounce @search, 500

    @results = new Backbone.Collection()

    @listenTo @results, 'selected', @setSelected
    @listenTo @results, 'reset', @addAll

    do @search

  search: ->
    catalogue = @$('#catalogue').val()
    term = @$('#term').val()

    if term.length > 0
      searchUrl = "/#{catalogue}/documents?term=state:published AND view:public AND NOT documentType:LINK_DOCUMENT AND #{term}"
    else
      searchUrl = "/#{catalogue}/documents?term=state:published AND view:public AND NOT documentType:LINK_DOCUMENT"

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
