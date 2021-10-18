define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    value: ''

  uris:
    environment: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/environment'
    information: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/geoscientificInformation'

  initialize: ->
    @on 'change:value', @updateUri

  updateUri: (model, value) ->
    @set 'uri', if @uris[value] then @uris[value] else ''