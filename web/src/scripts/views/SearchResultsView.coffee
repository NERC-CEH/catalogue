define [
  'underscore'
  'jquery'
  'backbone'
  'isInViewport'
], (_, $, Backbone) -> Backbone.View.extend
  events:
    "scroll" : "updateResultsOnScreen"

  initialize: ->

    results = _.map @$('.result'), (r) -> 
      title:       $('.title', r).text()
      description: $('.description', r).text()
      location:    $(r).attr('data-location')

    @collection.add results

  updateResultsOnScreen: ->
    results = @$ 'h2:in-viewport(.results)'
    firstResult = $(results[0]).parent()
    @$('.result').removeClass("selected")
    firstResult.addClass("selected")
    first = parseInt firstResult.attr('data-id'), 10
    @collection.setOnScreen first, results.length