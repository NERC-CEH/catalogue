define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  url: ->
    do @urlRoot

  urlRoot: ->
    "/documents/#{@id}/catalogue"

  options: [
      value: 'ceh'
      label: 'Centre for Ecology & hydrology'
    ,
      value: 'cmp'
      label: 'Catchment Management Platform'
    ,
      value: 'eidc'
      label: 'Environmental Information Data Centre'
  ]
