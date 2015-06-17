define [
  'jquery'
  'backbone'
  'cs!views/EditorView'
], ($, Backbone, EditorView) -> Backbone.View.extend

  initialize: ->
    if not @model
      throw new Error('model is required')

    if $('#metadata').length
      @setElement '#metadata'
    else
      @setElement '#search'

    @listenTo @model, 'loaded', @render

  render: ->
    view = new EditorView
      el: @el
      model: @model.getMetadata()
      parent: @model

    do view.render
    return @