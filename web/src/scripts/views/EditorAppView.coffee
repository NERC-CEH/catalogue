define [
  'underscore'
  'jquery'
  'backbone'
  'cs!views/editor/TitleView'
  'cs!views/editor/AbstractView'
  'cs!views/editor/PublicationDatesView'
  'cs!views/editor/RevisionDatesView'
  'cs!views/editor/LineageView'
  'tpl!templates/EditorApp.tpl'
], (_, $, Backbone, TitleView, AbstractView, PublicationDatesView, RevisionDatesView, LineageView, template) -> Backbone.View.extend

  events:
    'click #editorSave': 'save'
    'click #editorInfo': 'info'

  initialize: ->
    if $('#metadata').length
      @setElement '#metadata'
    else
      @setElement '#search'

    @listenTo @model, 'loaded', @render
    @listenTo @model.get('metadata'), 'save:error', (message) ->
      @model.trigger 'error', message
    @listenTo @model.get('metadata'), 'save:success', (message) ->
      @model.trigger 'info', message

  info: ->
    @model.trigger 'info', 'This is an info message'

  save: ->
    metadata = @model.get 'metadata'
    metadata.save {},
      success: (model) ->
        model.trigger 'save:success', "Successfully saved metadata"
      error: (model, response) ->
        model.trigger 'save:error', "Error saving metadata: #{response.status} (#{response.statusText})"

  render: ->
    @$el.html template
    metadata = @model.get 'metadata'

    title = new TitleView
      el: @$('#editorTitle')
      model: metadata

    abstract = new AbstractView
      el: @$('#editorAbstract')
      model: metadata

    publicationDates = new PublicationDatesView
      el: @$('#editorPublicationDates')
      model: metadata

    revisionDates = new RevisionDatesView
      el: @$('#editorRevisionDates')
      model: metadata

    lineage = new LineageView
      el: @$('#editorLineage')
      model: metadata