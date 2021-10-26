define [
  'underscore'
  'jquery'
  'backbone'
  'tpl!templates/SearchPage.tpl'
  'isInViewport'
], (_, $, Backbone, template) -> Backbone.View.extend

  initialize: ->
    do @readModelFromHTML

    @listenTo @model, 'cleared:results', @clear
    @listenTo @model, 'results-sync', @render
    @listenTo @model, 'results-change:selected', @updateSelected

    do @findSelected # Find selected, after @updateSelected has been registered
    do @padResults   # Change the padding of the results page

    # Ensure @ refers to this in the findSelected method. This means that we
    # can:
    #   - Pass @findSelected directly to the jquery.on method
    #   - Unbind @findSelected when the view is removed
    _.bindAll this, 'findSelected', 'padResults'
    $(window).on 'scroll', @findSelected
    $(window).on 'resize', @padResults

  ###
  Since we update the selected result based upon the scroll position, we need
  to be able to scroll all the way to select the last result. This method will
  ensure that the margin of the results view allows for such.
  ###
  padResults:->
    if @$('.result').length
      # Where the top result should start
      topPosition = $('.navbar').height() + $('.search-form').height()
      onScreen = @$('.result').last().nextAll().andSelf() # Elements to show
      onScreenHeight = _.reduce onScreen,
                                (height, e) -> height + $(e).outerHeight true,
                                topPosition

      @$el.css marginBottom : $(window).height() - onScreenHeight

  ###
  A page of search results may already be loaded in to the html. We can read
  that html and populate the model. If there is no html this method SHOULD NOT
  trigger a change on the model.
  ###
  readModelFromHTML:->
    @model.getResults().set
      numFound: @$('#num-records').val()
      results: _.map @$('.result'), (r) ->
        identifier:  $(r).attr('id')
        title:       $('.result__title', r).text()
        description: $('.result__description', r).text()
        locations:   $(r).attr('data-location').split '|'

  ###
  Event listener for when the selected id has changed on the search page model.
  Highlight that search result, with the selected class
  ###
  updateSelected: ->
    selected = @model.getResults().get 'selected'
    @$('.result').removeClass 'selected'
    @$("##{selected}").addClass 'selected'

  ###
  The following method will identify the result at the top of the screen and
  set the selected property on the @model. If there are no results, do nothing
  ###
  findSelected: ->
    if @$('.result').length
      offset = @$('.result .result__description').offset().top
      results = @$ ".result:in-viewport(#{offset})"
      # if no result was detected, default to the last result
      selected = if results.length then $(results[0]) else $('.result').last()
      @model.getResults().set selected: selected.attr 'id'

  ###
  Clear the dom of any content
  ###
  clear: -> do @$el.empty

  ###
  Draw in the new content
  ###
  render: ->
    @$el.html template @model.getResults().attributes
    $relatedSearches = @$('.results__related_searches')
    relatedSearches = @model.getResults().get('relatedSearches')
    console.log(relatedSearches.length)
    if relatedSearches.length > 0
      $relatedSearches.append('<h2>Related Searches</h2>')

    relatedSearches.forEach((relatedSearch, index) ->
      if index > 0
        prefix = ', '
      else
        prefix = ''
      $relatedSearches.append(
        "#{prefix}<a href=\"#{relatedSearch.href}\">#{relatedSearch.title}</a>"
      )
    )
    do @findSelected # Find the selected
    do @padResults   # Pad the results pane
