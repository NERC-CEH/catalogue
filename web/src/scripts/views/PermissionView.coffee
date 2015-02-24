define [
  'backbone'
  'tpl!templates/Permission.tpl'
  'cs!views/IdentityPermissionView'
  'cs!models/IdentityPermission'
], (Backbone, template, IdentityPermissionView, IdentityPermission) -> Backbone.View.extend
  el: '.permission'

  events:
    'click #permissionSave': 'save'
    'click #permissionAdd': 'add'

  initialize: ->
    @listenTo @model, 'sync', @reload
    @listenTo @model, 'permission:add', @addAll
    @listenTo @model, 'permission:remove', @addAll
    @listenTo @model, 'save:success', @leave

  save: ->
    console.log "saving permission: #{JSON.stringify @model.toJSON()}"
    @model.save {},
      success: =>
        @model.trigger 'save:success', "Save successful"
      error: (model, response) =>
        @model.trigger 'save:error', "Error saving permission: #{response.status} (#{response.statusText})"

  addOne: (permission) ->
    _.extend permission, parent: @model
    view = new IdentityPermissionView model: permission
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
    do @addAll

  add: ->
    identity = $('#identity').val()
    if identity
      permission = new IdentityPermission
        identity: identity
        canView: $('#canView').prop('checked')
        canEdit: $('#canEdit').prop('checked')
        canDelete: $('#canDelete').prop('checked')
      @model.addPermission permission

    $('#identity').val ""
    $('#canView').prop 'checked', false
    $('#canEdit').prop 'checked', false
    $('#canDelete').prop 'checked', false

  leave: ->
    console.log 'Bye'
    window.location.assign @model.get 'metadataHref'