define [
  'backbone'
  'cs!models/IdentityPermission'
], (Backbone, IdentityPermission) -> Backbone.Collection.extend
  model: IdentityPermission
  comparator: 'identity'