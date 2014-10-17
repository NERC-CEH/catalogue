define [
  'jquery'
  'backbone'
], ($, Backbone) -> Backbone.View.extend
  events:
    "click": "handleClick"

  ###
  This is the drawing control view. If not bounding box is currently set, it 
  will show a drawing tool so that a boundbox area can be selected
  ###
  initialize: ->
    do @render
    #@listenTo @model, 'change:bbox', @updateBBox
    @listenTo @model, 'change:drawing change:bbox', @render


  handleClick: ->
    if @model.has 'bbox'  # If the model already has a filter then remove it
      @model.unset 'bbox'
    else                  # Otherwise just toggle drawing mode
      @model.set 'drawing', not @model.get 'drawing'

  removeBbox: -> @model.unset 'bbox'

  render: ->
    @$el.removeClass 'active'

    if @model.get 'drawing'
      @$el.addClass 'active'
    else if @model.has 'bbox'
      @$el.html '''
      Spatial Filter
        <span class="glyphicon glyphicon-remove"></span>
      '''
    else
      @$el.html '''
        <span class="glyphicon glyphicon-pencil"></span>
      '''