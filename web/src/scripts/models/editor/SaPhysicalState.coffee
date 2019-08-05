define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    value: ''

  uris:
    'Air dried': 'http://vocab.ceh.ac.uk.org/esb#airdry'
    'Chemical extract': 'http://vocab.ceh.ac.uk.org/esb#chemicalExtract'
    'Chilled (refrigerated)': 'http://vocab.ceh.ac.uk.org/esb#chilled'
    'Fixed in formalin': 'http://vocab.ceh.ac.uk.org/esb#formalinFixed'
    'Formalin-Fixed Paraffin-Embedded (FFPE) tissue': 'http://vocab.ceh.ac.uk.org/esb#formalin-paraffin'
    'Freeze dried': 'http://vocab.ceh.ac.uk.org/esb#freezeDried'
    'Fresh': 'http://vocab.ceh.ac.uk.org/esb#fresh'
    'Frozen (-198 degrees C)': 'http://vocab.ceh.ac.uk.org/esb#frozen-198'
    'Frozen (-20 degrees C)': 'http://vocab.ceh.ac.uk.org/esb#frozen-20'
    'Frozen (-80 degrees C)': 'http://vocab.ceh.ac.uk.org/esb#frozen-80'
    'Natural state': 'http://vocab.ceh.ac.uk.org/esb#naturalState'
    'Oven dry': 'http://vocab.ceh.ac.uk.org/esb#ovendry'
    'Preserved': 'http://vocab.ceh.ac.uk.org/esb#preserved'
    'Preserved in alcohol': 'http://vocab.ceh.ac.uk.org/esb#preserved-in-alcohol'
    'Slide': 'http://vocab.ceh.ac.uk.org/esb#slide'
    'Taxidermy': 'http://vocab.ceh.ac.uk.org/esb#taxidermy'
    'Under liquid nitrogen': 'http://vocab.ceh.ac.uk.org/esb#liquidnitrogen'

  initialize: ->
    @on 'change:value', @updateUri

  updateUri: (model, value) ->
    @set 'uri', if @uris[value] then @uris[value] else ''