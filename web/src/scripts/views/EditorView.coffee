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
    @saveRequired = false

    @listenTo @model, 'error', (model, response) ->
      @$('#editorAjax').toggleClass 'visible'
      @$('#editorErrorMessage')
        .find('#editorErrorMessageResponse').text("#{response.status} #{response.statusText}")
        .end()
        .find('#editorErrorMessageJson').text(JSON.stringify model.toJSON())
        .end()
        .modal 'show'
    @listenTo @model, 'sync', ->
      @$('#editorAjax').toggleClass 'visible'
      @saveRequired = false
    @listenTo @model, 'change save:required', ->
      @saveRequired = true
    @listenTo @model, 'request', ->
      @$('#editorAjax').toggleClass 'visible'
    @listenTo @model, 'invalid', (model, errors) ->
      $modalBody = @$('#editorValidationMessage .modal-body')
      $modalBody.html ''
      _.each errors, (error) ->
        $modalBody.append @$("<p>#{error}</p>")
      @$('#editorValidationMessage').modal 'show'

    do @render
    _.invoke @sections[0].views, 'show'
    $editorNav = @$('#editorNav')
    _.each @sections, (section) ->
      $editorNav.append(@$("<li title='#{section.title}'>#{section.label}</li>"))

    $editorNav.find('li').first().addClass('active')

  attemptDelete: ->
    @$('#confirmDelete').modal 'show'

  delete: ->
    @$('#confirmDelete').modal 'hide'
    @model.destroy
      success: =>
        _.invoke @sections, 'remove'
        do @remove
        Backbone.history.location.replace '/documents'

  save: ->
    do @model.save

  attemptExit: ->
    if @saveRequired
      @$('#confirmExit').modal 'show'
    else
      do @exit

  exit: ->
    @$('#confirmExit').modal 'hide'
    _.invoke @sections, 'remove'
    do @remove

    catalogue = Backbone.history.location.pathname.split('/')[1]
    if Backbone.history.location.pathname == "/#{catalogue}/documents"
      Backbone.history.location.replace "/documents/#{@model.get 'id'}"
    else
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
