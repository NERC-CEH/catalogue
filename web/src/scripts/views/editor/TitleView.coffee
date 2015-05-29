define [
  'backbone'
  'tpl!templates/editor/Title.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change': 'updateModel'

  render: ->
    @$el.html template
      value: @model.get 'title'
    return @

  updateModel: ->
    value = @$("#input-title").val()
    if value
      @model.set 'title', value
    else
      @model.unset 'title'