define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  url: -> window.location.pathname

  defaults:
    results:  []
    prevPage: null
    nextPage: null

  ###
  This SearchPage may have the selected id populated. If it does, this method
  will return the full result which is selected. If nothing is selected, return
  undefined.
  ###
  getSelectedResult:->
    if @has 'results'
      return _.find @attributes.results, (e) => e.identifier is @attributes.selected
