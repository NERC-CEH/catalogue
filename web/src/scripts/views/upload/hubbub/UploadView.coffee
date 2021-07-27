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
    'click .load.datastore': 'loadDatastore'
    'click .load.documents': 'loadDropbox'
    'click .load.metadata': 'loadMetadata'
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

    @$dropbox = @$('.documents-files')
    @dropbox = new FileCollection()
    @listenTo(@dropbox, 'reset', (collection) -> @addAll(collection, @$dropbox))
    @listenTo(@dropbox, 'add', (model) -> @addOne(@dropbox, @$dropbox, model))
    @listenTo(@dropbox, 'update', () -> @showEmptyStorage(@dropbox, @$dropbox, 'documents'))

    @$metadata = @$('.metadata-files')
    @metadata = new FileCollection()
    @listenTo(@metadata, 'reset', (collection) -> @addAll(collection, @$metadata))
    @listenTo(@metadata, 'add', (model) -> @addOne(@metadata, @$metadata, model))
    @listenTo(@metadata, 'update', () -> @showEmptyStorage(@metadata, @$metadata, 'metadata'))

    $datastoreData = $('#datastore-data')
    @datastore.reset(JSON.parse($datastoreData.text())) if $datastoreData.length

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

  loadMore: (event, name, path, collection) ->
    currentClasses = @showInProgress(event)
    nextPage = @model.get("#{name}Page") + 1
    size = @model.get("#{name}Size")
    $.ajax({
      url: "#{@model.url()}/#{path}?page=#{nextPage}&size=#{size}"
      success: (data) =>
        @showNormal(event, currentClasses)
        collection.add(data)
        if data.length == size
          @model.set("#{name}Page", nextPage)
      error: (err) ->
        @showInError(event)
        console.error('error', err)
    })

  loadDatastore: (event) ->
    @loadMore(event, 'datastore', 'eidchub', @datastore)

  loadDropbox: (event) ->
    @loadMore(event, 'dropbox', 'dropbox', @dropbox)

  loadMetadata: (event) ->
    @loadMore(event, 'metadata', 'supporting-documents', @metadata)

  showEmptyStorage: (collection, $container, title) ->
    if (collection.length == 0)
      $container.append("<h3 class=\"no-documents text-center\">NO FILES IN #{title.toUpperCase()}</h3>")
    else
      $container.find('.no-documents').remove()

  showModal: (title, body, action, event) ->
    @showInProgress(event)
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
      () -> @finish(event),
      event
    )

  finish: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax
      url: "#{@model.url()}/finish"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
        window.location.assign("/documents/#{@model.get('id')}")
      error: (err) ->
        @showInError(event)
        console.error('error', err)

  moveAllDatastore: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax
      url: "#{@model.url()}/move-all-datastore"
      type: 'POST'
      success: =>
        @dropbox.each (model) => @addOne(@datastore, @$datastore, model.copy('/eidchub/'))
        @dropbox.reset()
        @showNormal(event, currentClasses)
      error: (err) =>
        @showInError(event)
        console.error('error', err)

  reschedule: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax
      url: "#{@model.url()}/reschedule"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
#        TODO: re-render based on changed state of model rather than reloading
        window.location.reload()
      error: (err) =>
        @showInError(event)
        console.error('error', err)

  schedule: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax
      url: "#{@model.url()}/schedule"
      type: 'POST'
      success: =>
        @showNormal(event, currentClasses)
#        TODO: re-render based on changed state of model rather than reloading
        window.location.reload()
      error: (err) =>
        @showInError(event)
        console.error('error', err)

  validateAll: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax
      url: "#{@model.url()}/validate"
      type: 'POST'
      success: =>
        showNormal = () => @showNormal(event, currentClasses)
        datastore = () => @getServerState('datastore', 'eidchub', @datastore, showNormal)
        dropbox = () => @getServerState('dropbox', 'dropbox', @dropbox, datastore)
        metadata = () => @getServerState('metadata', 'supporting-documents', @metadata, dropbox)
        setTimeout(metadata, 7000)
      error: (err) =>
        @showInError(event)
        console.error('error', err)

  getServerState: (name, path, collection, callback) ->
    page = 1
    collectionSize = collection.length
    size = @model.get("#{name}Size")
    size = collectionSize if collectionSize > size
    @model.set("#{name}Page": page)
    @model.set("#{name}Size": size)

    $.ajax
      url: "#{@model.url()}/#{path}?page=#{page}&size=#{size}"
      dataType: 'json'
      success: (data) ->
        collection.reset(data)
        callback() if callback
