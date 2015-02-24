define [
  'backbone'
  'tpl!templates/IdentityPermission.tpl'
], (Backbone, template) -> Backbone.View.extend
  tagName: 'tr'

  events:
    'click button': 'removePermission'
    'click [type="checkbox"]': 'update'

  removePermission: ->
    console.dir @
    do @parent.removePermission @model

  update: (event) ->
    permission = $(event.target).data('permission')
    @model.set permission, not @model.get(permission)

  render: ->
    @$el.html template @model.toJSON()
    return @