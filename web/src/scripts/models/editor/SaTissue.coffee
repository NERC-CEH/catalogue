define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    value: ''

  uris:
    'Bone': 'http://vocab.ceh.ac.uk.org/esb#bone'
    'Brain': 'http://vocab.ceh.ac.uk.org/esb#brain'
    'Egg': 'http://vocab.ceh.ac.uk.org/esb#egg'
    'Fat': 'http://vocab.ceh.ac.uk.org/esb#fat '
    'Feather': 'http://vocab.ceh.ac.uk.org/esb#feather'
    'Fur': 'http://vocab.ceh.ac.uk.org/esb#fur'
    'Gut contents': 'http://vocab.ceh.ac.uk.org/esb#stomachContents'
    'Heart': 'http://vocab.ceh.ac.uk.org/esb#heart'
    'Homogenised whole sample': 'http://vocab.ceh.ac.uk.org/esb#Homogenised'
    'Kidney': 'http://vocab.ceh.ac.uk.org/esb#kidney'
    'Liver': 'http://vocab.ceh.ac.uk.org/esb#liver'
    'Lung': 'http://vocab.ceh.ac.uk.org/esb#lung'
    'Lymph node': 'http://vocab.ceh.ac.uk.org/esb#lymphNode'
    'Muscle': 'http://vocab.ceh.ac.uk.org/esb#muscle'
    'Nerve/spinal cord': 'http://vocab.ceh.ac.uk.org/esb#nerve-spinalcord'
    'Plasma': 'http://vocab.ceh.ac.uk.org/esb#plasma'
    'Serum': 'http://vocab.ceh.ac.uk.org/esb#serum'
    'Skin ': 'http://vocab.ceh.ac.uk.org/esb#skin'
    'Spleen': 'http://vocab.ceh.ac.uk.org/esb#spleen'
    'Trachea': 'http://vocab.ceh.ac.uk.org/esb#trachea'
    'Whole blood': 'http://vocab.ceh.ac.uk.org/esb#blood'
    'Whole body': 'http://vocab.ceh.ac.uk.org/esb#carcase'

  initialize: ->
    @on 'change:value', @updateUri

  updateUri: (model, value) ->
    @set 'uri', if @uris[value] then @uris[value] else ''