define [
  'backbone'
  'cs!views/PermissionView'
], (Backbone, PermissionView) -> Backbone.View.extend
  el: '.permission'

  initialize: ->
    @listenTo @model, 'loaded', @render

  render: ->
    view = new PermissionView
     model: @model.getPermission()
    do view.render
    return @