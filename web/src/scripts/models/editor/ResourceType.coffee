define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    value: ''

  uris:
    dataset: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/dataset'
    series: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/series'
    service: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/services'

  initialize: ->
    @on 'change:value', @updateUri

  updateUri: (model, value) ->
    @set 'uri', if @uris[value] then @uris[value] else ''