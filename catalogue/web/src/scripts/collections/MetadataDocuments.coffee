define [
  'backbone'
  'cs!models/MetadataDocument'
], (Backbone, MetadataDocument) -> Backbone.Collection.extend
  model: MetadataDocument

  ###
  Takes the list of ids and sets these onto this model
  ###
  setById: (ids, options) -> @set _.map( ids, (id)-> id: id), options