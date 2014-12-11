define [
  'jquery'
  'underscore'
  'backbone'
  'cs!models/Metadata'
], ($, _, Backbone, Metadata) -> Backbone.Router.extend
  routes:
    'edit/new': 'newRecord'
    'edit/:identifier' : 'loadRecord'

  initialize: (options) ->
    console.log "initializing EditorRouter with model: #{JSON.stringify options.model.toJSON()}"
    @model = options.model

  loadRecord: (identifier) ->
    console.log "Router loading record: #{identifier}"
    metadata = new Metadata
      id: identifier

    metadata.on 'change', -> console.log 'change'

    metadata.on 'add:accessConstraints', (model) -> console.log "added access constraint: #{JSON.stringify model.toJSON()}"

    metadata.fetch
      success: (model) ->

        console.log "Access Constraints 0: #{JSON.stringify model.get 'accessConstraints'}"

        model.get('accessConstraints').add 'new restrictions'
        console.log "Access Constraints 1: #{JSON.stringify model.get 'accessConstraints'}"

        model.set 'boundingBoxes.0.westBoundLongitude', -24.56

        console.log "Bounding Boxes #{JSON.stringify model.get 'boundingBoxes'}"

        model.set 'datasetReferenceDate.creationDate', '2013-12-23'

        do model.save

  newRecord: ->
    console.log 'new record'