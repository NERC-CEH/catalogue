define [
  'underscore'
  'jquery'
  'backbone'
  'tpl!templates/SearchPage.tpl'
  'isInViewport'
], (_, $, Backbone, template) -> Backbone.View.extend

  initialize: ->
    console.log 'Creating a search page ' + @model.cid
    do @readModelFromHTML

    @listenTo @model, 'sync', @render
    @listenTo @model, 'change:selected', @updateSelected

    do @findSelected # Find selected, after @updateSelected has been registered

    # Ensure @ refers to this in the findSelected method. This means that we 
    # can:
    #   - Pass @findSelected directly to the jquery.on method
    #   - Unbind @findSelected when the view is removed
    _.bindAll this, 'findSelected'
    $(window).on 'scroll', @findSelected
  
  ###
  A page of search results may already be loaded in to the html. We can read
  that html and populate the model. If there is no html this method SHOULD NOT
  trigger a change on the model.
  ###
  readModelFromHTML:->
    @model.set 
      numFound: @$('#num-records').val()
      results: _.map @$('.result'), (r) -> 
        identifier:  $(r).attr('id')
        title:       $('.title', r).text()
        description: $('.description', r).text()
        locations:   $(r).attr('data-location')

  ###
  Event listener for when the selected id has changed on the search page model.
  Highlight that search result, with the selected class
  ###
  updateSelected: ->
    @$('.result').removeClass 'selected'
    @$("##{@model.get 'selected' }").addClass 'selected'


  ###
  The following method will identify the result at the top of the screen and
  set the selected property on the @model. If there are no results, do nothing
  ###
  findSelected: ->
    if @$('.result').length
      offset = @$('.result .description').offset().top
      results = @$ ".result:in-viewport(#{offset})"
      @model.set selected: $(results[0]).attr 'id'


  render: ->
    @$el.html template @model.attributes
    do @findSelected # Find the selected
    
  ###
  Override the view remove method, when called we need to unbind the scroll
  listener from the window
  ###
  remove: ->
    $(window).off 'scroll', @findSelected
    Backbone.View.prototype.remove.apply this, arguments

