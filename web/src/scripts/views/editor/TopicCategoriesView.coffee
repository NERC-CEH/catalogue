define [
  'backbone'
  'tpl!templates/editor/TopicCategories.tpl'
  'tpl!templates/editor/TopicCategory.tpl'
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
      'change select': 'modify'

    render: ->
      @$el.html itemlTemplate
      @$('select').val @model.get 'value'
      return @

    remove: ->
      @model.collection.remove @model

    modify: ->
      @model.set 'value', @$(':selected').val()
      @model.collection.trigger 'modify'

  View = Backbone.View.extend

    events:
      'click #topicCategoryAdd': 'add'

    initialize: ->
      if not @model
        throw new Error('model is required')

      @topicCategories = new Collection()

      _.each @model.get('topicCategories'), (topic) =>
        @topicCategories.add new Model
          value: topic

      @listenTo @topicCategories, 'add remove modify', @updateModel

    addOne: (topicCategory) ->
      view = new ItemView model: topicCategory
      @$('tbody').append view.render().el

    addAll: ->
      @$('tbody').html('')
      @topicCategories.each @addOne, @

    render: ->
      @$el.html template
      do @addAll
      return @

    add: ->
      topicCategory = @$('#topicCategory').val()
      if topicCategory
        @topicCategories.add new Model
          value: topicCategory

      $('#topicCategory').val ""

    updateModel: ->
      @model.set 'topicCategories', @topicCategories.map (model) -> model.get 'value'
      do @addAll

  return View