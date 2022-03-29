define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    value: null

  uris:
    'instrumentedSites':'http://vocabs.ceh.ac.uk/ri/instrumentedSites'
    'surveys':'http://vocabs.ceh.ac.uk/ri/surveys'
    'wildlifeSchemes':'http://vocabs.ceh.ac.uk/ri/wildlifeSchemes'
    'discoveryCollections':'http://vocabs.ceh.ac.uk/ri/discoveryCollections'
    'mobilePlatforms':'http://vocabs.ceh.ac.uk/ri/mobilePlatforms'
    'controlledPlatforms':'http://vocabs.ceh.ac.uk/ri/controlledPlatforms'
    'fieldPlatforms':'http://vocabs.ceh.ac.uk/ri/fieldPlatforms'
    'labs':'http://vocabs.ceh.ac.uk/ri/labs'

  classes:
    'instrumentedSites':'Environmental observatories'
    'surveys':'Environmental observatories'
    'wildlifeSchemes':'Environmental observatories'
    'discoveryCollections':'Environmental observatories'
    'mobilePlatforms':'Environmental experiment platforms'
    'controlledPlatforms':'Environmental experiment platforms'
    'fieldPlatforms':'Environmental experiment platforms'
    'labs':'Environmental analysis'
  
  descriptions:
    'instrumentedSites':'Instrumented sites'
    'surveys':'Surveys'
    'wildlifeSchemes':'Wildlife monitoring schemes'
    'discoveryCollections':'Discovery collections'
    'mobilePlatforms':'Mobile observing platforms'
    'controlledPlatforms':'Controlled environment platforms'
    'fieldPlatforms':'Field research platforms'
    'labs':'Analytical labs'

  initialize: ->
    @on 'change:value', @updateLimitation

  updateLimitation: (model, value) ->
    @set 'uri', if @uris[value] then @uris[value] else null
    @set 'infrastructureClass', if @classes[value] then @classes[value] else null
    @set 'description', if @descriptions[value] then @descriptions[value] else null