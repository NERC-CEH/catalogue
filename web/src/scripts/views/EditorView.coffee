define [
  'underscore'
  'jquery'
  'backbone'
  'tpl!templates/Editor.tpl'
  'cs!views/MessageView'
  'cs!views/editor/TitleView'
  'cs!views/editor/ResourceTypeView'
  'cs!views/editor/AlternateTitlesView'
  'cs!views/editor/DescriptionView'
  'cs!views/editor/LineageView'
  'cs!views/editor/TopicCategoriesView'
  'cs!views/editor/ContactsView'
  'cs!views/editor/ResourceIdentifiersView'
], (_, $, Backbone, template, MessageView, TitleView, ResourceTypeView, AlternateTitlesView, DescriptionView, LineageView, TopicCategoriesView, ContactsView, ResourceIdentifiersView) -> Backbone.View.extend

  events:
    'click #editorDelete': 'delete'
    'click #editorExit': 'exit'
    'click #editorSave': 'save'
    'click #editorBack': 'back'
    'click #editorNext': 'next'
    'click #editorNav li': 'direct'

  initialize: ->
    @listenTo @model, 'error', (model, response) ->
      alert "Problem communicating with server: #{response.status} (#{response.statusText})"

    @currentStep = 1
    do @render
    @components = [
      new TitleView
        el: @$('#editorTitle')
        model: @model,
      new ResourceTypeView
        el: @$('#editorResourceType')
        model: @model,
      new AlternateTitlesView
        el: @$('#editorAlternateTitles')
        model: @model,
      new DescriptionView
        el: @$('#editorDescription')
        model: @model,
      new LineageView
        el: @$('#editorLineage')
        model: @model,
      new TopicCategoriesView
        el: @$('#editorTopicCategories')
        model: @model,
      new ContactsView
        el: @$('#editorContacts')
        model: @model,
      new ResourceIdentifiersView
        el: @$('#editorResourceIdentifiers')
        model: @model
    ]

  delete: ->
    do @model.destroy

  save: ->
    @model.save {},
      error: (model, response) ->
        console.log "Error saving metadata: #{response.status} (#{response.statusText})"

  exit: ->
    _.invoke @components, 'remove'
    do @remove
    do Backbone.history.location.reload

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