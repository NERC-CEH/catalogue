define [
  'underscore'
  'backbone'
  'tpl!templates/Permission.tpl'
  'cs!views/IdentityPermissionView'
  'cs!models/IdentityPermission'
], (_, Backbone, template, IdentityPermissionView, IdentityPermission) -> Backbone.View.extend
  el: '.permission'

  events:
    'click #permissionSave': 'save'
    'click #permissionAdd': 'add'

  initialize: ->
    @listenTo @model, 'sync', @reload
    @listenTo @model, 'all', (name) ->
      console.log "event: #{name}"

  save: ->
    console.log "saving permission: #{JSON.stringify @model.toJSON()}"
    @model.save {},
      success: =>
        @model.trigger 'save:success', "Save successful"
      error: (model, response) =>
        @model.trigger 'save:error', "Error saving permission: #{response.status} (#{response.statusText})"

  addOne: (permission) ->
#    console.log "identity permission view for: #{JSON.stringify permission.toJSON()}"
    view = new IdentityPermissionView
      model: permission
      parent: @model
    @$('tbody').append view.render().el

  addAll: ->
    @$('tbody').html('')
    @model.get('permissions').each @addOne, @

  render: ->
    @$el.html template @model.toJSON()
    do @addAll
    return @

  reload: ->
    do @model.loadCollection
    do @render

  add: ->
    permission = new IdentityPermission identity: "joe"
    @model.addPermission permission