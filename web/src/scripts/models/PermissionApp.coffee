define [
  'underscore'
  'backbone'
  'cs!models/Permission'
], (_, Backbone, Permission) -> Backbone.Model.extend

  loadPermission: (identifier) ->
    permission = new Permission
      id: identifier

    permission.fetch
      success: (model) =>
        console.log "Success loading permission for: #{model.id}"
        console.log "Permission: #{JSON.stringify model.toJSON()}"
        @set 'permission', model
        @trigger 'loaded'
      error: (model) =>
        console.log "Error loading permission for: #{model.id}"
        @trigger 'error', "Unable to load permission for: #{model.id}"

  getPermission: ->
    _.clone(@get 'permission')