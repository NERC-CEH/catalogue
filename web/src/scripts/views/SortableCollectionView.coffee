define [
  'jquery'
  'underscore'
  'backbone'
  'jquery-ui/sortable'
], ($, _, Backbone) -> Backbone.View.extend

  events:
    "sortstart":  "sortstart"
    "sortupdate": "sortUpdate"

  initialize: ->
    @listenTo @collection, 'add',    @add
    @listenTo @collection, 'remove', @remove
    @listenTo @collection, 'reset',  @reset
    
    do @$el.sortable

  ###
  Create an instance of the subview as specified in this views attributes and
  append this subview to the supplied model for easy access later. 

  WARNING: This will mean that we can only add this model to one of these types
  of views
  ###
  add: (model) -> 
    model._subView = new @attributes.subView
      model: model
      el: $('<li class="list-group-item"></li>').prependTo(@$el)

  ###
  Remove the given models subview
  ###
  remove: (model) -> do model._subView.remove

  ###
  Scan over all the old models and remove the subviews then add the new ones
  ###
  reset: (models, options) ->
    _.each options.previousModels, (model) => @remove model
    models.forEach (model) => @add model

  ###
  Event handler for when a sorting starts. We will store the position that
  the given element was in
  ###
  sortStart: (event, ui) ->
    @_oldPosition = @collection.length - 1 - ui.item.index()

  ###
  Event handler for when sorting has finished. Call the positioning method
  of the collection. NOTE we don't listen to updates in the position in this 
  view
  ###
  sortUpdate: (event, ui) ->
    newPosition = @collection.length - 1 - ui.item.index()
    @collection.position @_oldPosition, newPosition