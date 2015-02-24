define [
  'backbone'
], (Backbone) -> Backbone.Model.extend
  idAttribute: "identity"

  defaults:
    canView: false
    canEdit: false
    canDelete: false

  remove: ->
    console.log "remove #{JSON.stringify @toJSON()}"
    @collection.remove @