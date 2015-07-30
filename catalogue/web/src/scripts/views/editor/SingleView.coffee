define [
  'backbone'
  'tpl!templates/editor/Single.tpl'
], (Backbone, template) -> Backbone.View.extend

  className: 'component'

  initialize: (options) ->
    @data = options
    if not @data.ModelType
      @data.ModelType = Backbone.Model

  show: ->
    @$el.addClass 'visible'

  hide: ->
    @$el.removeClass 'visible'

  render: ->
    @$el.html template  data: @data
    @

  updateMetadataModel: (attribute) ->
    @model.set @data.modelAttribute, attribute