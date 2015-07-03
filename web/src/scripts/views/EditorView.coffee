define [
  'underscore'
  'backbone'
  'tpl!templates/Editor.tpl'
], (_, Backbone, template) -> Backbone.View.extend

  events:
    'click #editorDelete': 'attemptDelete'
    'click #confirmDeleteYes': 'delete'
    'click #editorExit': 'attemptExit'
    'click #exitWithoutSaving': 'exit'
    'click #editorSave': 'save'
    'click #editorBack': 'back'
    'click #editorNext': 'next'
    'click #editorNav li': 'direct'

  initialize: ->
    @currentStep = 1
    @listenTo @model, 'error', @errorMessage
    @listenTo @model, 'sync', @noSaveRequired
    @listenTo @model, 'change save:required', @saveRequired
    _.invoke @sections[0].views, 'show'
    @saveRequired = false
    do @render

  errorMessage: (model, response) ->
    @$('#editorErrorMessage')
      .find('#editorErrorMessageResponse').text("#{response.status} #{response.statusText}")
      .end()
      .find('#editorErrorMessageJson').text(JSON.stringify model.toJSON())
      .end()
      .modal 'show'

  saveRequired: ->
    @saveState true

  noSaveRequired: ->
    @saveState false

  saveState: (state) ->
    @saveRequired = state
    @$('#editorSave').prop 'disabled', not @saveRequired

  attemptDelete: ->
    @$('#confirmDelete').modal 'show'

  delete: ->
    @$('#confirmDelete').modal 'hide'
    do @model.destroy

  save: ->
    @model.save {},
      error: (model, response) ->
        console.log "Error saving metadata: #{response.status} (#{response.statusText})"

  attemptExit: ->
    if @saveRequired
      @$('#confirmExit').modal 'show'
    else
      do @exit

  exit: ->
    @$('#confirmExit').modal 'hide'
    _.invoke @sections, 'remove'
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