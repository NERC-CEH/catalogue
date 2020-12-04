define [
  'underscore'
  'jquery'
  'cs!views/ExtentHighlightingMapView'
], (_, $, ExtentHighlightingMapView) -> ExtentHighlightingMapView.extend
  el: '#studyarea-map'

  initialize: ->
    ExtentHighlightingMapView.prototype.initialize.call this, arguments #Initialize super
    do @render

  ###
  Grab the locations off of the map element
  ###
  getLocations: ->
    wkts = @$('[dataType="geo:wktLiteral"]')
    return _.map wkts, (el) -> $(el).attr 'content'

  ###
  Update the highlighted areas based upon the locations. Then zoom the to
  highlighted regions
  ###
  render:->
    @setHighlighted @getLocations()
    do @zoomToHighlighted
