define [
  'backbone'
], (Backbone) -> Backbone.Router.extend

  routes:
    'permission/:identifier': 'loadPermission'

  initialize: (options) ->
    @model = options.model

  loadPermission: (identifier) ->
    @model.loadPermission identifier