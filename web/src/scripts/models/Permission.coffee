define [
  'underscore'
  'backbone'
  'cs!collections/IdentityPermissions'
  'cs!models/IdentityPermission'
], (_, Backbone, IdentityPermissions, IdentityPermission) -> Backbone.Model.extend

  url: ->
    do @urlRoot

  urlRoot: ->
    "/documents/#{@id}/permission"

  loadCollection: ->
    collection = new IdentityPermissions()
    collection.reset @get 'permissions'
    @set 'permissions', collection

  addPermission: (permission) ->
    collection = _.clone @get 'permissions'
    collection.add permission
    @set 'permissions', collection
    @trigger "permission:add"

  removePermission: (permission) ->
    collection = _.clone @get 'permissions'
    collection.remove permission
    @set 'permissions', collection
    @trigger "permission:remove"