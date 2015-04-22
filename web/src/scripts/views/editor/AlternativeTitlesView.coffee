define [
  'backbone'
  'tpl!templates/editor/AlternativeTitles.tpl'
  'tpl!templates/editor/AlternativeTitle.tpl'
], (Backbone, template, itemlTemplate) ->

  Model = Backbone.Model.extend
    defaults:
      value: ''

  Collection = Backbone.Collection.extend
    model: Model

  ItemView = Backbone.View.extend
    tagName: 'tr'

    events:
      'click button': 'remove'

    render: ->
      @$el.html itemlTemplate @model.toJSON()
      return @

    remove: ->
      @model.collection.remove()

  view = Backbone.View.extend

    events:
      'click #alternativeTitleAdd': 'add'

    initialize: ->
      if not @model
        throw new Error('model is required')

      @alternativeTitles = new Collection()

      _.each @model.get('alternativeTitles'), (altTitle) ->
        @alternativeTitles.add new Model
          value: altTitle

      @alternativeTitles.add new Model
        value: "this is a test"

      @delegateEvents()

      @listenTo @alternativeTitles, 'change', @updateModel
      @listenTo @alternativeTitles, 'all', (name) ->
        console.log "from alternativeTitles: #{name}"

    addOne: (alternativeTitle) ->
      view = new ItemView model: alternativeTitle
      @$('tbody').append view.render().el

    addAll: ->
      @$('tbody').html('')
      @alternativeTitles.each @addOne, @

    render: ->
      @$el.html template
      do @addAll
      return @

    add: ->
      alternativeTitle = $('#alternativeTitleAdd').val()
      if alternativeTitle
        @alternativeTitles.add new Model
          value: alternativeTitle

    updateModel: ->
      @model.set 'alternativeTitles', @alternativeTitles.map (model) -> model.get 'value'

  return view