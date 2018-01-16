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
        do model.loadCollection
        @set 'permission', model
        @trigger 'loaded'

      error: (model) =>
        @trigger 'error', "Unable to load permission for: #{model.id}"

  getPermission: ->
    _.clone(@get 'permission')