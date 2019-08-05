define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    value: ''

  uris:
    'Air': 'http://vocab.ceh.ac.uk.org/esb#air'
    'DNA': 'http://vocab.ceh.ac.uk.org/esb#dna'
    'Ectoparasite': 'http://vocab.ceh.ac.uk.org/esb#ectoparasite'
    'Endoparasite': 'http://vocab.ceh.ac.uk.org/esb#endoparasite'
    'Fossil': 'http://vocab.ceh.ac.uk.org/esb#fossil'
    'Fresh water': 'http://vocab.ceh.ac.uk.org/esb#freshWater'
    'Gas': 'http://vocab.ceh.ac.uk.org/esb#gas'
    'Ice core': 'http://vocab.ceh.ac.uk.org/esb#iceCore'
    'Pathogen': 'http://vocab.ceh.ac.uk.org/esb#pathogen'
    'Rain water': 'http://vocab.ceh.ac.uk.org/esb#rain'
    'RNA': 'http://vocab.ceh.ac.uk.org/esb#rna'
    'Rock': 'http://vocab.ceh.ac.uk.org/esb#rock'
    'Sea water': 'http://vocab.ceh.ac.uk.org/esb#seaWater'
    'Sediment': 'http://vocab.ceh.ac.uk.org/esb#sediment'
    'Seed': 'http://vocab.ceh.ac.uk.org/esb#seed'
    'Soil': 'http://vocab.ceh.ac.uk.org/esb#soil'
    'Surface water': 'http://vocab.ceh.ac.uk.org/esb#surfaceWater'
    'Vegetation': 'http://vocab.ceh.ac.uk.org/esb#Vegetation'

  initialize: ->
    @on 'change:value', @updateUri

  updateUri: (model, value) ->
    @set 'uri', if @uris[value] then @uris[value] else ''