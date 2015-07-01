define [
  'underscore'
  'backbone'
  'tpl!templates/Editor.tpl'
], (_, Backbone, template) -> Backbone.View.extend

  events:
    'click #editorDelete': 'attemptDelete'
    'click #editorExit': 'exit'
    'click #editorSave': 'save'
    'click #editorBack': 'back'
    'click #editorNext': 'next'
    'click #editorNav li': 'direct'

  initialize: ->
    @currentStep = 1
    @listenTo @model, 'error', (model, response) ->
      alert "Problem communicating with server: #{response.status} (#{response.statusText})"
    @listenTo @model, 'change save:required', @toggleSave
    _.invoke @sections[0].views, 'show'
    do @render

  toggleSave: ->
    @$('#editorSave').prop 'disabled', (i, current) -> not current

  attemptDelete: ->
    if confirm "Delete metadata?"
      do @model.destroy

  save: ->
    @model.save {},
      error: (model, response) ->
        console.log "Error saving metadata: #{response.status} (#{response.statusText})"

  exit: ->
    reallyExit = =>
      _.invoke @sections, 'remove'
      do @remove
      do Backbone.history.location.reload

    if not @$('#editorSave').prop 'disabled'
      if confirm "Exit without saving changes to metadata?"
        do reallyExit
    else
      do reallyExit

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
    $nav = @$('#editorNav li')
    maxStep = $nav.length
    @currentStep = newStep
    @currentStep = 1 if @currentStep < 1
    @currentStep = maxStep if @currentStep > maxStep

    $back = @$('#editorBack')
    if @currentStep == 1
      $back.prop 'disabled', true
    else
      $back.prop 'disabled', false

    $next = @$('#editorNext')
    if @currentStep == maxStep
      $next.prop 'disabled', true
    else
      $next.prop 'disabled', false

    $nav.filter('.active').toggleClass 'active'
    @$($nav[@currentStep - 1]).toggleClass 'active'

    _.each @sections, (section, index) =>
      method = 'hide'
      if (@currentStep - 1) == index
        method = 'show'
      _.invoke section.views, method

  render: ->
    @$el.html template
    _.each @sections, (section) ->
      _.each section.views, (view) ->
        @$('#editor').append view.el
    @