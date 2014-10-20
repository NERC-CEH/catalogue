define [
  'jquery'
  'backbone'
  'tpl!templates/FacetsPanel.tpl'
  'tpl!templates/FacetResults.tpl'
], ($, Backbone, panelTpl, resultsTpl) -> Backbone.View.extend

  initialize: ->
    @listenTo @model, 'results-change', @render

  render: ->
    if @model.getResults()
      @$el.html panelTpl 
        facets:   @model.getResults().attributes.facets
        template: resultsTpl