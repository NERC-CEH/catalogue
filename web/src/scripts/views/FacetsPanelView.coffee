define [
  'jquery'
  'backbone'
  'tpl!templates/FacetsPanel.tpl'
  'tpl!templates/FacetResults.tpl'
], ($, Backbone, panelTpl, resultsTpl) -> Backbone.View.extend

  initialize: ->
    @listenTo @model, 'results-sync', @render

  ###
  Render the facet results panel as long as we have some results currently set.

  The template panelTpl requires a sub template which renders the each facet 
  results set.
  ###
  render: ->
    @$el.html panelTpl 
      facets:   @model.getResults().attributes.facets
      template: resultsTpl