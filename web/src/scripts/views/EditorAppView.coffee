define [
  'underscore'
  'jquery'
  'backbone'
  'cs!models/editor/Textarea'
  'cs!views/editor/TextareaView'
  'cs!views/editor/PublicationDatesView'
  'cs!views/editor/RevisionDatesView'
  'tpl!templates/EditorApp.tpl'
  'text!help/Title.html'
  'text!help/Abstract.html'
  'text!help/Lineage.html'
], (_, $, Backbone, Textarea, TextareaView, PublicationDatesView, RevisionDatesView, template, titleHelp, abstractHelp, lineageHelp) -> Backbone.View.extend

  events:
    'click #editorSave': 'save'
    'click #editorInfo': 'info'

  initialize: ->
    if $('#metadata').length
      @setElement '#metadata'
    else
      @setElement '#search'

    @listenTo @model, 'loaded', @render
    @listenTo @model, 'save:error', (message) ->
      @model.trigger 'error', message
    @listenTo @model, 'save:success', (message) ->
      @model.trigger 'info', message
    @listenTo @model, 'all', (evt) ->
      console.log "event '#{evt}' fired"
    @listenTo @model, 'change:metadata', (model, value, options) ->
      console.dir model
      console.dir value
      console.dir options
      console.log "metadata changed: #{JSON.stringify model.toJSON()}"

  info: ->
    @model.trigger 'info', 'This is an info message'

  save: ->
    metadata = @model.getMetadata()
    metadata.save {},
      success: =>
        @model.trigger 'save:success', "Save successful"
      error: (model, response) =>
       @model.trigger 'save:error', "Error saving metadata: #{response.status} (#{response.statusText})"

  render: ->
    @$el.html template
    metadata = @model.getMetadata()

    title = new TextareaView
      el: @$('#editorTitle')
      model: new Textarea
        id: 'title'
        name: 'Title'
        rows: 2
        required: 'required'
        value: metadata.get 'title'
        help: titleHelp

    abstract = new TextareaView
      el: @$('#editorAbstract')
      model: new Textarea
        id: 'description'
        name: 'Description'
        required: 'required'
        value: metadata.get 'description'
        help: abstractHelp

#    publicationDates = new PublicationDatesView
#      el: @$('#editorPublicationDates')
#      model: metadata
#
#    revisionDates = new RevisionDatesView
#      el: @$('#editorRevisionDates')
#      model: metadata

    lineage = new TextareaView
      el: @$('#editorLineage')
      model: new Textarea
        id: 'lineage'
        name: 'Lineage'
        value: metadata.get 'lineage'
        help: lineageHelp