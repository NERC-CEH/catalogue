define [
  'underscore'
  'backbone'
  'tpl!templates/editor/ChildLarge.tpl'
], (_, Backbone, template) -> Backbone.View.extend

  className: 'row'

  events:
    'click button.remove': 'delete'
    'click button.showhide': 'showhide'

  initialize: (options) ->
    @data = options
    @listenTo @model, 'remove', -> do @remove
    @listenTo @model, 'showhide', -> do @showhide
    @index = @model.collection.indexOf @model
    do @render
    new @data.ObjectInputView _.extend {}, @data,
      el: @$('.dataentry')
      model: @model
      index: @index

  render: ->
    @$el.html template index: @index, data: @data
    @

  delete: ->
    @model.collection.remove @model

  showhide: ->
    if @$('.extended').hasClass('hidden')
      @$('.extended').removeClass 'hidden'
      @$('.showhide span').removeClass 'fa-chevron-down'
      @$('.showhide span').addClass 'fa-chevron-up'
    else
      @$('.extended').addClass 'hidden'
      @$('.showhide span').removeClass 'fa-chevron-up'
      @$('.showhide span').addClass 'fa-chevron-down'

