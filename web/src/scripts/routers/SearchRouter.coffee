define [
  'jquery'
  'underscore'
  'backbone'
  'jquery-deparam'
], ($, _, Backbone, deparam) -> Backbone.Router.extend
  routes:
    "*data" : "updateModel"

  initialize: (options) ->
    @model = options.model

    # If there is no hash component, we can use the query string to update the
    # model to represent the state of the document which is already loaded
    if not options.location.hash
      @updateModel options.location.search.substring(1), silent: true

    @model.on 'change', => do @updateRoute

  ###
  Gets the state of the model and turns it into a query state string which this
  router will be able to parse and process at a later time
  ###
  updateRoute: ->
    queryString = $.param @model.getState(), true
    @navigate queryString, replace:true

  ###
  Updates the model given the specified state object. Options can be passed to 
  avoid unnessersary triggering of events
  ###
  updateModel: (state, options) -> 
    @model.setState(deparam(state, true), options) if state