define [
  'underscore'
  'backbone'
  'tpl!templates/PermissionApp.tpl'
], (_, Backbone, template) -> Backbone.View.extend
  el: '.permission'

  events:
    'click #permissionSave': 'save'

  initialize: ->

    @listenTo @model, 'loaded', @render
    @listenTo @model, 'save:error', (message) ->
      @model.trigger 'error', message
    @listenTo @model, 'save:success', (message) ->
      @model.trigger 'info', message
    @listenTo @model, 'change:permission', (model) ->
      console.log "permission changed: #{JSON.stringify model.toJSON()}"

  save: ->
    permission = @model.getPermission()
    permission.save {},
      success: =>
        @model.trigger 'save:success', "Save successful"
      error: (model, response) =>
        @model.trigger 'save:error', "Error saving permission: #{response.status} (#{response.statusText})"

  render: ->
    permission = @model.getPermission().attributes

    @$el.html template
      title: permission.title
      permissions: _.sortBy permission.permissions, 'identity'
      href: permission.metadataHref