define [
  'jquery'
  'backbone'
  'jquery-deparam'
  'cs!views/SearchFormView'
  'cs!views/SpatialSearchView'
  'cs!views/SearchPageView'
  'cs!views/DrawingControlView'
  'cs!views/FacetsPanelView'
], ($, Backbone, deparam, SearchFormView, SpatialSearchView, SearchPageView, DrawingControlView, FacetsPanelView) -> Backbone.View.extend
  el: '#search'

  events:
    "click .facet-heading h3": "disableSearchMap"
    "click .map-heading h3":   "enableSearchMap"

  initialize: (options)->
    @appUrl = window.location.href.split('#')[0].split('?')[0]

    # Mutate the events hash so that is listens to clicks of urls which will
    # update the state of this web application
    @events["click a[href='#{@appUrl}']"] = 'defaultState'
    @events["click a[href^='#{@appUrl}?']"] = 'handleUrl'
    @delegateEvents @events # Register the mutated events object

    do @render
    @listenTo @model, 'change:mapsearch', @updateSearchMap
    do @updateFilters


  ###
  The Sample Archive (and possibly other catalogues) does not have any facets
  to filter by.  So make the Map Search the default tab when there are no facets
  and hide the Filter search
  ###
  updateFilters: ->
    if (!$($('.facet-filter')[0]).has("h3").length)
      do @enableSearchMap
      do @$('.facet-heading').hide
      @$('.map-filter').css('top', @$('.map-heading').css('height'))
      @$('.map-heading').css('margin-top', '0px')

  ###
  Grab the query state from the anchors clicked href. Use this to update the
  models state
  ###
  handleUrl: (e) ->
    query = $(e.currentTarget).attr('href').split('?')[1]
    @model.setState deparam(query, true)
    do e.preventDefault

  ###
  Set an empty state object on the model. This will revert the application to
  the default state.
  ###
  defaultState: (e)-> 
    @model.setState {}
    do e.preventDefault

  ###
  Disable the search map on the model and therefore enable the facet view
  ###
  disableSearchMap: -> @model.set 'mapsearch', false

  ###
  Enable the search map on the model. This will hide the facet view
  ###
  enableSearchMap: -> @model.set 'mapsearch', true

  ###
  Update the state of this view, we will either start showing the search map or
  the facet pane
  ###
  updateSearchMap: ->
    newMode = if @model.get 'mapsearch' then 'map-mode' else 'facets-mode'
    @$el.removeClass('facets-mode').removeClass('map-mode').addClass newMode
    do @spatialSearchView.refresh

  ###
  Create the sub views of the search web application
  ###
  render: ->
    @searchFormView = new SearchFormView
      model: @model
      el:    @$('.search-form')
      
    @spatialSearchView = new SpatialSearchView
      model: @model
      el:    @$('.openlayers')

    @searchResultsView = new SearchPageView
      model: @model
      el:    @$('.results')

    @drawingControlView = new DrawingControlView
      model: @model
      el:    @$('.drawing-control')

    @facetsPanelView = new FacetsPanelView
      model: @model
      el:    @$('.facet-filter')
