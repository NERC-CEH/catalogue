define [
  'underscore'
  'backbone'
  'tpl!templates/Editor.tpl'
  'cs!views/MessageView'
  'cs!views/editor/TitleView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/ResourceTypeView'
  'cs!views/editor/AlternateTitlesView'
  'cs!views/editor/DescriptionView'
  'cs!views/editor/LineageView'
  'cs!views/editor/TopicCategoriesView'
  'cs!views/editor/ContactsView'
  'cs!views/editor/ResourceIdentifiersView'
  'cs!views/editor/DatasetReferenceDateView'
], (_, Backbone, template, MessageView, TitleView, SingleObjectView, ResourceTypeView, AlternateTitlesView, DescriptionView, LineageView, TopicCategoriesView, ContactsView, ResourceIdentifiersView, DatasetReferenceDateView) -> Backbone.View.extend

  events:
    'click #editorDelete': 'attemptDelete'
    'click #editorExit': 'exit'
    'click #editorSave': 'save'
    'click #editorBack': 'back'
    'click #editorNext': 'next'
    'click #editorNav li': 'direct'

  initialize: ->
    @listenTo @model, 'error', (model, response) ->
      alert "Problem communicating with server: #{response.status} (#{response.statusText})"

    @listenTo @model, 'change save:required', @toggleSave

    @currentStep = 1
    do @render
    @components = [
      new TitleView
        el: @$('#editorTitle')
        model: @model

      new SingleObjectView
        el: @$('#editorResourceType')
        model: @model
        modelAttribute: 'resourceType'
        label: 'Resource Type'
        ObjectInputView: ResourceTypeView,
        helpText: """
                  Type of resource.
                  """

      new SingleObjectView
        el: @$('#editorDatasetReferenceDate')
        model: @model
        modelAttribute: 'datasetReferenceDate'
        label: 'Dataset Reference Date'
        ObjectInputView: DatasetReferenceDateView,
        helpText: """
                  <p>Creation date, the date the data resource is created.</p>
                  <p>The publication date is the date when the data resource is being made available or released for use - it is <strong>NOT</strong> the date of creation.</p>
                  <p>If you include a revision date, it implies that the resource has been changed as a consequence of edits or updates.  For EIDC Hub records it is usual practice for revised resources to have an entirely new record, therefore <em>revision date</em> is rarely necessary.</p>
                  """

      new AlternateTitlesView
        el: @$('#editorAlternateTitles')
        model: @model

      new DescriptionView
        el: @$('#editorDescription')
        model: @model

      new LineageView
        el: @$('#editorLineage')
        model: @model

      new TopicCategoriesView
        el: @$('#editorTopicCategories')
        model: @model

      new ContactsView
        el: @$('#editorContacts')
        model: @model

      new ResourceIdentifiersView
        el: @$('#editorResourceIdentifiers')
        model: @model
    ]

  toggleSave: ->
    @$('#editorSave').prop 'disabled', (i, current) -> not current

  attemptDelete: ->
    if confirm "Delete metadata?"
      do @model.destroy

  save: ->
    @model.save {},
      success: ->
        do @toggleSave
      error: (model, response) ->
        console.log "Error saving metadata: #{response.status} (#{response.statusText})"

  exit: ->
    reallyExit = =>
      _.invoke @components, 'remove'
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

    $step = @$('.step')
    $step.filter('.visible').toggleClass 'visible'
    @$($step[@currentStep - 1]).toggleClass 'visible'

  render: ->
    @$el.html template
    @