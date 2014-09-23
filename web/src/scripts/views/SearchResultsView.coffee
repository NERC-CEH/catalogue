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
    results = @$ '.result:in-viewport'
    first = parseInt $(results[0]).attr('data-id'), 10
    @collection.setOnScreen first, results.length