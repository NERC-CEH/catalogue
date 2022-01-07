define [
  'underscore'
  'jquery'
  'backbone'
], (_, $, Backbone) -> Backbone.View.extend
  events:
    "keyup  [name='term']": "handleTyping"
    "change [name='term']": "handleTyping"
    "paste  [name='term']": "handleTyping"
    "input  [name='term']": "handleTyping"
    "submit":               "handleSubmit"

  initialize: ->
    # Create a method which waits until after input has completed before 
    # actually setting the displayed term to the model
    @updateTermOnModelWhenComplete = _.debounce @updateTermOnModel, 500

    @listenTo @model, 'change:term', @updateDisplayedTerm

  ###
  Event listener for changed input in the search term box. This will instantly
  clear the results (as these will now be dirty). After input has stopped, the
  term will be set on the model
  ###
  handleTyping: ->
    if @getDisplayedTerm() isnt @model.get 'term'
      do @model.clearResults
      do @updateTermOnModelWhenComplete

  ###
  The search form has been submitted. We can update the search term right away
  ###
  handleSubmit:(e)->
    do @updateTermOnModel
    do e.preventDefault

  ###
  Reads the current term from the search box and sets it onto the model
  ###
  updateTermOnModel:-> @model.set 'term', @getDisplayedTerm()

  ###
  Obtains the current term from the search box
  ###
  getDisplayedTerm:-> @$("[name='term']").val()

  ###
  Update the term box based upon the content in the model
  ###
  updateDisplayedTerm: -> 
    term = @model.get 'term'
    displayed = $("[name='term']").val()
    $("[name='term']").val term if term isnt displayed
