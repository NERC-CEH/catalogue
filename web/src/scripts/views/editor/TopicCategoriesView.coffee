define [
  'backbone'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/TopicCategoriesItemView'
  'tpl!templates/editor/TopicCategories.tpl'
], (Backbone, TopicCategory, TopicCategoriesItemView, template) -> Backbone.View.extend

  events:
    'change #topicCategory': 'add'

  initialize: ->
    if not @model
      throw new Error('model is required')

    @topicCategories = new Backbone.Collection @model.get('topicCategories'), TopicCategory

    @listenTo @topicCategories, 'add remove change', @updateModel

  addOne: (topicCategory) ->
    view = new TopicCategoriesItemView model: topicCategory
    @$('#topicCategories').append view.render().el

  addAll: ->
    @$('#topicCategories').html('')
    @topicCategories.each @addOne, @

  render: ->
    @$el.html template
    do @addAll
    return @

  add: ->
    $topicCategory = @$('#topicCategory')
    value = $topicCategory.val()

    if value
      topicCategory = new TopicCategory()
      topicCategory.set 'value': value # need to force 'set' to update uri
      @topicCategories.add topicCategory

    $topicCategory.val ""

  updateModel: ->
    if @topicCategories.length > 0
      @model.set 'topicCategories', @topicCategories
    else
      @model.unset 'topicCategories'
    do @addAll