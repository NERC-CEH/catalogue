define [
  'jquery'
  'cs!views/ExtentHighlightingMapView'
], ($, ExtentHighlightingMapView) -> ExtentHighlightingMapView.extend
  el: '#studyarea-map'

  initialize: ->
    ExtentHighlightingMapView.prototype.initialize.call this, arguments #Initialize super
    do @render

  ###
  Grab the locations off of the map element
  ###
  getLocations: -> @$el.attr('content').split ','

  ###
  Update the highlighted areas based upon the locations. Then zoom the to 
  highlighted regions
  ###
  render:->
    @setHighlighted @getLocations()
    do @zoomToHighlighted
