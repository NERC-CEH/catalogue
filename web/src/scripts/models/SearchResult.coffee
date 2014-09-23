define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend
 
  ###
  Get the title of this search result
  ###
  getTitle: -> @get "title"

  ###
  Get the title of this search result
  ###
  getDescription: -> @get "description"

  ###
  Get the locations of this search result
  ###
  getLocations: -> 
    [minx,miny,maxx,maxy] = @get('location').split ' '

    return "POLYGON((#{minx} #{miny}, #{minx} #{maxy}, #{maxx} #{maxy}, #{maxx} #{miny}, #{minx} #{miny}))"