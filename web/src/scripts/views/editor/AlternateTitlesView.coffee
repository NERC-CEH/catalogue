define [
  'backbone'
  'tpl!templates/editor/alternateTitles.tpl'
  'tpl!templates/editor/alternateTitle.tpl'
], (Backbone, template, itemlTemplate) ->
  ENTER_KEY = 13

  Model = Backbone.Model.extend
    defaults:
      value: ''

  Collection = Backbone.Collection.extend
    model: Model

  ItemView = Backbone.View.extend
    tagName: 'tr'

    events:
      'click button': 'remove'
      'change input': 'modify'

    render: ->
      @$el.html itemlTemplate @model.toJSON()
      return @

    remove: ->
      @model.collection.remove @model

    modify: ->
      @model.set 'value', @$(':input').val()
      @model.collection.trigger 'modify'

  View = Backbone.View.extend

    events:
      'click #alternateTitleAdd': 'add'
      'keydown #alternateTitle': 'addEnter'

    initialize: ->
      if not @model
        throw new Error('model is required')

      @alternateTitles = new Collection()

      _.each @model.get('alternateTitles'), (altTitle) =>
        @alternateTitles.add new Model
          value: altTitle

      @listenTo @alternateTitles, 'add remove modify', @updateModel

    addOne: (alternateTitle) ->
      view = new ItemView model: alternateTitle
      @$('tbody').append view.render().el

    addAll: ->
      @$('tbody').html('')
      @alternateTitles.each @addOne, @

    render: ->
      @$el.html template
      do @addAll
      return @

    addEnter: (event) ->
      if event.keyCode == ENTER_KEY
        do @add

    add: ->
      alternateTitle = @$('#alternateTitle').val()
      if alternateTitle
        @alternateTitles.add new Model
          value: alternateTitle

      $('#alternateTitle').val ""

    updateModel: ->
      @model.set 'alternateTitles', @alternateTitles.map (model) -> model.get 'value'
      do @addAll

  return View