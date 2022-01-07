define [
  'backbone'
], (Backbone) -> Backbone.Model.extend
  idAttribute: "identity"

  defaults:
    canView: false
    canEdit: false
    canDelete: false
    canUpload: false