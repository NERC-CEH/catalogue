define [
  'backbone'
  'tpl!templates/editor/AlternativeTitles.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change': 'updateModel'

  addOne: (permission) ->
    _.extend permission, parent: @model
    view = new IdentityPermissionView model: permission
    @$('tbody').append view.render().el

  addAll: ->
    @$('tbody').html('')
    @model.get('permissions').each @addOne, @

  render: ->
    @$el.html template
    return @

  updateModel: ->
    @model.set 'title', @$("#input-title").val()