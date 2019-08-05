define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    value: ''

  uris:
    'Algae': 'http://vocab.ceh.ac.uk.org/esb#algae'
    'Amphibian': 'http://vocab.ceh.ac.uk.org/esb#amphibian'
    'Annelid': 'http://vocab.ceh.ac.uk.org/esb#annelid'
    'Arthropod': 'http://vocab.ceh.ac.uk.org/esb#arthropod'
    'Bacteria': 'http://vocab.ceh.ac.uk.org/esb#bacteria'
    'Bird': 'http://vocab.ceh.ac.uk.org/esb#bird'
    'Fern': 'http://vocab.ceh.ac.uk.org/esb#fern'
    'Fish': 'http://vocab.ceh.ac.uk.org/esb#fish'
    'Fungi': 'http://vocab.ceh.ac.uk.org/esb#fungi'
    'Invertebrate': 'http://vocab.ceh.ac.uk.org/esb#invertebrate'
    'Lichen': 'http://vocab.ceh.ac.uk.org/esb#lichen'
    'Mammal': 'http://vocab.ceh.ac.uk.org/esb#mammal'
    'Mollusc': 'http://vocab.ceh.ac.uk.org/esb#mollusc'
    'Moss or liverwort': 'http://vocab.ceh.ac.uk.org/esb#moss'
    'Nematode': 'http://vocab.ceh.ac.uk.org/esb#nematode'
    'Plankton': 'http://vocab.ceh.ac.uk.org/esb#plankton'
    'Plant': 'http://vocab.ceh.ac.uk.org/esb#plant'
    'Platyhelminthe': 'http://vocab.ceh.ac.uk.org/esb#platyhelminthe'
    'Protozoa': 'http://vocab.ceh.ac.uk.org/esb#protozoa'
    'Reptile': 'http://vocab.ceh.ac.uk.org/esb#reptile'
    'Virus': 'http://vocab.ceh.ac.uk.org/esb#virus'


  initialize: ->
    @on 'change:value', @updateUri

  updateUri: (model, value) ->
    @set 'uri', if @uris[value] then @uris[value] else ''