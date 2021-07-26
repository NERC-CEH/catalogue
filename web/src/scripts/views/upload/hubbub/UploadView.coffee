define [
  'jquery'
  'backbone'
  'cs!collections/upload/hubbub/FileCollection'
  'cs!models/upload/hubbub/File'
  'cs!views/upload/hubbub/FileView'
  'tpl!templates/upload/hubbub/FileRow.tpl'
  'cs!views/upload/hubbub/DropzoneView'
], ($, Backbone, FileCollection, File, FileView, fileRowTemplate, DropzoneView) -> Backbone.View.extend

  events:
    'click .finish': 'showFinish'
    'click .load.documents': 'loadDropbox'
    'click .move-all': 'moveAllDatastore'
    'click .reschedule': 'reschedule'
    'click .schedule': 'schedule'
    'click .validate-all': 'validateAll'

  initialize: ->
    if @$('.dropzone-container').length
      dropzoneSuccess = (file) =>
        model = new File
          bytes: file.size
          name: file.name
          path: "/dropbox/#{@model.get('id')}/#{file.name}"
          status: 'WRITING'
          check: true
        @addOne(@dropbox, @$dropbox, model)
        $(file.previewElement).remove()

      new DropzoneView
        el: '.dropzone-container'
        success: dropzoneSuccess
        url: @model.url()

    @$datastore = @$('.datastore-files')
    @datastore = new FileCollection()
    @listenTo(@datastore, 'reset', (collection) -> @addAll(collection, @$datastore))
    @listenTo(@datastore, 'add', (model) -> @addOne(@datastore, @$datastore, model))
    @listenTo(@datastore, 'update', () -> @showEmptyStorage(@datastore, @$datastore, 'datastore'))
    @listenTo(@datastore, 'all', (name) -> console.log("all: #{name}"))

    @$dropbox = @$('.documents-files')
    @dropbox = new FileCollection()
    @listenTo(@dropbox, 'reset', (collection) -> @addAll(collection, @$dropbox))
    @listenTo(@dropbox, 'add', (model) -> @addOne(@dropbox, @$dropbox, model))
    @listenTo(@dropbox, 'update', () -> @showEmptyStorage(@dropbox, @$dropbox, 'documents'))
    @listenTo(@dropbox, 'all', (name) -> console.log("all: #{name}"))

    @$metadata = @$('.metadata-files')
    @metadata = new FileCollection()
    @listenTo(@metadata, 'reset', (collection) -> @addAll(collection, @$metadata))
    @listenTo(@metadata, 'add', (model) -> @addOne(@metadata, @$metadata, model))
    @listenTo(@metadata, 'update', () -> @showEmptyStorage(@metadata, @$metadata, 'metadata'))
    @listenTo(@metadata, 'all', (name) -> console.log("all: #{name}"))

    $datastoreData = $('#datastore-data')
    @datastore.reset(JSON.parse(@$datastore.text())) if $datastoreData.length

    $dropboxData = $('#dropbox-data')
    @dropbox.reset(JSON.parse($dropboxData.text())) if $dropboxData.length

    $metadataData = $('#metadata-data')
    @metadata.reset(JSON.parse($metadataData.text())) if $metadataData.length

  addOne: (collection, $container, model) ->
    view = new FileView
      collection: collection
      datastore: @datastore
      metadata: @metadata
      model: model
      url: @model.url()
    $container.append(view.render().el)

  addAll: (collection, $container) ->
    $container.empty()
    collection.each((model) => @addOne(collection, $container, model))

  loadDropbox: (event) ->
    currentClasses = @showInProgress(event)
    nextPage = @model.get('dropboxPage') + 1
    size = @model.get('dropboxSize')
    $.ajax({
      url: "#{@model.url()}/dropbox?page=#{nextPage}&size=#{size}"
      success: (data) =>
        @showNormal(event, currentClasses)
        numModels = data.length
        if numModels > 0
          @dropbox.add(data)

        if numModels == size
          @model.set('dropboxPage', nextPage)
        else
          $(event.currentTarget)
            .attr('disabled', true)
      error: (err) ->
        @showInError(event)
        console.error('error', err)
    })

  showEmptyStorage: (collection, $container, title) ->
    if (collection.length == 0)
      console.log("Adding empty to #{title}")
      $container.append("<h3 class=\"no-documents text-center\">NO FILES IN #{title.toUpperCase()}</h3>")
    else
      console.log("removing empty from #{title}")
      $container.find('.no-documents').remove()

  showModal: (title, body, action) ->
    $modal = $('#documentUploadModal')
    $('.modal-title', $modal).html(title)
    $('.modal-body', $modal).html(body)
    $('.modal-accept', $modal).unbind('click')
    $('.modal-accept', $modal).click(action.bind(@))
    $modal.modal('show')

  showInProgress: (event) ->
    $el = $(event.currentTarget)
    $el.attr('disabled', true)
    $icon = $('i', $el)
    current = $icon.attr('class')
    $icon.attr('class', 'btn-icon fas fa-sync fa-spin')
    current

  showNormal: (event, classes) ->
    $el = $(event.currentTarget)
    $el.attr('disabled', false)
    $icon = $('i', $el)
    $icon.attr('class', classes)

  showInError: (event) ->
    $el = $(event.currentTarget)
    $el.attr('disabled', true)
    $icon = $('i', $el)
    $icon.attr('class', 'btn-icon fa fa-exclamation-triangle')

  showFinish: (event) ->
    @showModal(
      'Have you finished uploading files?',
      'You will no longer be able to add, remove or update files.',
      @finish(event)
    )

  finish: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/finish"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
        window.location.assign("/documents/#{@model.get('id')}")
      error: (err) ->
        @showInError(event)
        console.error('error', err)
    })

  moveAllDatastore: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/move-all-datastore"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
#        TODO: remove all the models from dropbox collection and add then to datastore collection
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })

  reschedule: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/reschedule"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
#        TODO: re-render based on changed state of model rather than reloading
        window.location.reload()
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })

  schedule: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/schedule"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
#        TODO: re-render based on changed state of model rather than reloading
        window.location.reload()
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })

  validateAll: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax({
      url: "#{@model.url()}/validate"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
      error: (err) =>
        @showInError(event)
        console.error('error', err)
    })
