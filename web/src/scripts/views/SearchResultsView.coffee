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


    @firstResultPosition = @$('.result .description').offset().top

    do @updateResultsOnScreen
    $(window).scroll => do @updateResultsOnScreen

  updateResultsOnScreen: ->
    results = $ ".result:in-viewport(#{@firstResultPosition})"
    firstResult = $(results[0])

    #if firstResult.length
    @$('.result').removeClass("selected")
    firstResult.addClass("selected")
    first = parseInt firstResult.attr('data-id'), 10

    @collection.setOnScreen first, results.length