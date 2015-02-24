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

    models = _.map @get('permissions'), (attrs) ->
      new IdentityPermission attrs, collection: collection

    collection.reset models
    @set 'permissions', collection

    console.log "after collection loaded: #{JSON.stringify @toJSON()}"

  addPermission: (permission) ->
    collection = @get 'permissions'
    extended = _.extend permission, collection: collection
    collection.add extended
    @set 'permissions', collection

    console.log "after permission added: #{JSON.stringify @toJSON()}"

  removePermission: (permission) ->
    collection = _.clone @get 'permissions'
    collection.remove permission
    @set 'permissions', collection

    console.log "after permission removed: #{JSON.stringify @toJSON()}"
