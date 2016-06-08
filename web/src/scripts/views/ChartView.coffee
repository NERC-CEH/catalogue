define [
  'underscore'
  'jquery'
  'backbone'
  'chartjs'
], (_, $, Backbone, Chart) -> Backbone.View.extend
  initialize: ->
    data = @$('li').map (i, el) => 
      value: $('span.badge', el).html()
      color: $('span.badge', el).css('background-color')
      label: $('span.legend-label', el).html()

    ctx = @$('canvas')[0].getContext '2d'

    @chart = new Chart(ctx).Pie data,
      segmentStrokeWidth: 0
      segmentStrokeColor: "#000000"
      responsive:         true
