define [
  'backbone'
  'cs!modoels/MetadataDocument'
], (Backbone, MetadataDocument) -> Backbone.Collection.extend
  model: MetadataDocument