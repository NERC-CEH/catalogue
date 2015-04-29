define [
  'backbone'
], (Backbone) ->
  uris =
    dataset: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/dataset'
    series: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/series'
    service: 'http://inspire.ec.europa.eu/metadata-codelist/ResourceType/services'

  Backbone.Model.extend ->


