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
    'click #editorBack': 'back'
    'click #editorNext': 'next'

  initialize: ->
    if $('#metadata').length
      @setElement '#metadata'
    else
      @setElement '#search'

    @currentStep = 1

    @listenTo @model, 'loaded', @render
    @listenTo @model, 'save:error', (message) ->
      @model.trigger 'error', message
    @listenTo @model, 'save:success', (message) ->
      @model.trigger 'info', message
    @listenTo @model, 'change:metadata', (model) ->
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

  back: ->
    @navigate -1

  next: ->
    @navigate 1

  navigate: (direction) ->
    $nav = $('#editorNav li')
    maxStep = $nav.length
    @currentStep += direction
    @currentStep = 1 if @currentStep < 1
    @currentStep = maxStep if @currentStep > maxStep

    $back = $('#editorBack')
    if @currentStep == 1
      $back.prop 'disabled', true
    else
      $back.prop 'disabled', false

    $next = $('#editorNext')
    if @currentStep == maxStep
      $next.prop 'disabled', true
    else
      $next.prop 'disabled', false

    $nav.filter('.active').toggleClass 'active'
    $($nav[@currentStep - 1]).toggleClass 'active'

    $step = $('#editor .step')
    $step.filter('.visible').toggleClass 'visible'
    $($step[@currentStep - 1]).toggleClass 'visible'

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
        parent: @model

    abstract = new TextareaView
      el: @$('#editorAbstract')
      model: new Textarea
        id: 'description'
        name: 'Description'
        required: 'required'
        value: metadata.get 'description'
        help: abstractHelp
        parent: @model

#    publicationDates = new PublicationDatesView
#      el: @$('#editorPublicationDates')
#      model: metadata
#
#    revisionDates = new RevisionDatesView
#      el: @$('#editorRevisionDates')
#      model: metadata

    lineage = new TextareaView
      el: @$('#editorLineage')
      parent: @model
      model: new Textarea
        id: 'lineage'
        name: 'Lineage'
        value: metadata.get 'lineage'
        help: lineageHelp
        parent: @model