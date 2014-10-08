define [
  'underscore'
  'backbone'
  'cs!collections/SearchResults'
], (_, Backbone, SearchResults) -> Backbone.Model.extend

  defaults:
    searchResults: new SearchResults

  getSearchResults: -> @get 'searchResults'