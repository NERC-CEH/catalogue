define [
  'underscore'
  'jquery'
  'backbone'
  'tpl!templates/Editor.tpl'
  'cs!views/MessageView'
  'cs!views/editor/TitleView'
  'cs!views/editor/AlternateTitlesView'
  'cs!views/editor/DescriptionView'
  'cs!views/editor/LineageView'
], (_, $, Backbone, template, MessageView, TitleView, AlternateTitlesView, DescriptionView, LineageView) -> Backbone.View.extend

  events:
    'click #editorSave': 'save'
    'click #editorBack': 'back'
    'click #editorNext': 'next'
    'click #editorNav li': 'direct'

  initialize: (options) ->
    if not @model
      throw new Error('model is required')
    if not (options and options.parent)
      throw new Error('parent is required')
    @parent = options.parent

    @listenTo @model, 'save:success', @leave

    @currentStep = 1

  save: ->
    @model.save {},
      success:  =>
        @model.trigger "save:success"
      error: (model, response) =>
        @parent.trigger 'error', "Error saving metadata: #{response.status} (#{response.statusText})"

  leave: ->
    window.location.assign @model.get 'uri'

  back: ->
    @navigate @currentStep - 1

  next: ->
    @navigate @currentStep + 1

  direct: (event) ->
    node = event.currentTarget
    step = 0
    while node != null
      step++
      node = node.previousElementSibling

    @navigate step

  navigate: (newStep) ->
    $nav = $('#editorNav li')
    maxStep = $nav.length
    @currentStep = newStep
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

    title = new TitleView
      el: @$('#editorTitle')
      model: @model
    do title.render

    alternateTitles = new AlternateTitlesView
      el: @$('#editorAlternateTitles')
      model: @model
    do alternateTitles.render

    description = new DescriptionView
      el: @$('#editorDescription')
      model: @model
    do description.render

    lineage = new LineageView
      el: @$('#editorLineage')
      model: @model
    do lineage.render

#    publicationDates = new PublicationDatesView
#      el: @$('#editorPublicationDates')
#      model: metadata
#
#    revisionDates = new RevisionDatesView
#      el: @$('#editorRevisionDates')
#      model: metadata

#    lineage = new TextareaView
#      el: @$('#editorLineage')
#      model: new Textarea
#        id: 'lineage'
#        name: 'Lineage'
#        value: @model.get 'lineage'
#        help: lineageHelp
#        parent: @model

#    additionalInformation = new TextareaView
#      el: @$('#editorAdditionalInformation')
#      model: new Textarea
#        id: 'additionalInformation'
#        name: 'Additional Information'
#        value: @model.get 'additionalInformation'
#        parent: @model

# should be a list of string
#    accessConstraints = new TextareaView
#      el: @$('#editorAccessConstraints')
#      model: new Textarea
#        id: 'accessConstraints'
#        name: 'Access Constraints'
#        value: metadata.get 'accessConstraints'
#        parent: @model