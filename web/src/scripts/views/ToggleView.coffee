define [
  'jquery'
  'backbone'
], ($, Backbone) -> Backbone.View.extend
  events:
    "click :checkbox" : "updateModel"

  ###
  Create a new view based around a toggle. Updates to this toggle shall 
  manipulate the @property value of @model
  ###
  initialize: (options) ->
    @property = options.property
    do @render
    @listenTo @model, @property, @render #Rerender if model changes

  ###
  Updates the value of the model based upon the current checked state of the 
  checkbox
  ###
  updateModel: -> @model.set @property, @$(':checkbox').is ':checked'

  ###
  Updates the state of the spatial search checkbox by the @property value
  defined in the @model of this view
  ###
  render:-> @$(':checkbox').prop 'checked', @model.get @property