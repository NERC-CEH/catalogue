define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    value: ''

  uris:
    'Biodiversity': 'http://vocab.ceh.ac.uk/ri#Biodiversity'
    'Pollution': 'http://vocab.ceh.ac.uk/ri#Pollution'
    'Climate change resilience': 'http://vocab.ceh.ac.uk/ri#Resilience'
    'Climate change mitigation': 'http://vocab.ceh.ac.uk/ri#Mitigation'


  initialize: ->
    @on 'change:value', @updateUri

  updateUri: (model, value) ->
    @set 'uri', if @uris[value] then @uris[value] else ''
