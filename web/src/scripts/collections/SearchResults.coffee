define [
  'backbone'
  'cs!models/SearchResult'
], (Backbone, SearchResult) -> Backbone.Collection.extend
  model: SearchResult

  initialize:->
    @onscreen = new Backbone.Model

  getResultsOnScreen: -> 
    @slice @onscreen.get('start'), @onscreen.get('end')

  setOnScreen: (start, length) ->
    @onscreen.set start: start, end: start + length