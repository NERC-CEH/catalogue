define [
  'underscore'
  'backbone'
  'cs!collections/Layers'
  'cs!collections/SearchResults'
], (_, Backbone, Layers, SearchResults) -> Backbone.Model.extend

  defaults:
    layers: new Layers
    searchResults: new SearchResults

  getLayers: -> @get 'layers'

  getSearchResults: -> @get 'searchResults'