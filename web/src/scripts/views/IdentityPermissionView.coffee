define [
  'backbone'
  'tpl!templates/IdentityPermission.tpl'
], (Backbone, template) -> Backbone.View.extend
  tagName: 'tr'

  events:
    'click button': 'remove'
    'click [type="checkbox"]': 'update'

  remove: ->
    do @model.destroy

  update: (event) ->
    permission = $(event.target).data('permission')
    @model.set permission, not @model.get(permission)

  render: ->
    @$el.html template @model.attributes