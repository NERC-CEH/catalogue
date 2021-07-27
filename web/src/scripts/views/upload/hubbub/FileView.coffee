define [
  'jquery'
  'backbone'
  'cs!models/upload/hubbub/File'
  'tpl!templates/upload/hubbub/FileRow.tpl'
], ($, Backbone, File, template) -> Backbone.View.extend

  defaults:
    check: false

  events:
    'click .panel-heading': 'expand'
    'click .accept': 'accept'
    'click .cancel': 'showCancel'
    'click .delete': 'showDelete'
    'click .ignore': 'ignore'
    'click .move-datastore': 'moveDatastore'
    'click .move-metadata': 'moveMetadata'
    'click .validate': 'validate'

  template: template

  initialize: (options) ->
    @url = options.url
    @datastore = options.datastore
    @metadata = options.metadata
    @listenTo(@model, 'change', @render)
    if @model.get('check')
      setTimeout(
        () => @getServerState(),
        7000
      )

  getServerState: (callback) ->
    $.ajax
      url: "#{@url}?path=#{encodeURIComponent(@model.get('path'))}"
      dataType: 'json'
      success: (data) =>
        @model.update(data)
        callback() if callback
      error: (err) ->
        console.error('error', err)

# TODO: turn modal into a view as used in multiple places
  showModal: (title, body, action) ->
    $modal = $('#documentUploadModal')
    $('.modal-title', $modal).html(title)
    $('.modal-body', $modal).html(body)
    $('.modal-accept', $modal).unbind('click')
    $('.modal-accept', $modal).click(action.bind(@))
    $modal.modal('show')

  accept: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax
      url: "#{@url}/accept?path=#{encodeURIComponent(@model.get('path'))}"
      type: 'POST'
      success: =>
        setTimeout(
          () =>
            @getServerState(() ->  @showNormal(event, currentClasses))
          ,
          3000
        )
      error: (err) =>
        @showInError(event)
        console.error('error', err)

  showCancel: (event) ->
    @showInProgress(event)
    @showModal(
      "Cancel moving #{@model.get('name')}?",
      'This will not stop the file from being moved.<br/>Only do this if you feel the file is no longer moving to the desired destination,<br/>e.g. due to a server error.',
      () -> @cancel(event)
    )

  cancel: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax
      url: "#{@url}/cancel?path=#{encodeURIComponent(@model.get('path'))}"
      type: 'POST'
      success: =>
        setTimeout(
          () =>
            @getServerState(() -> @showNormal(event, currentClasses))
          ,
            3000
        )
      error: (err) =>
        @showInError(event)
        console.error('error', err)

  showDelete: (event) ->
    @showInProgress(event)
    @showModal(
      "Delete #{@model.get('name')}?",
      "This will permanently delete the file<br/><b>#{@model.get('path')}</b>",
      () -> @delete(event)
    )

  showIgnore: (event) ->
    @showInProgress(event)
    @showModal(
      "Ignore the error for #{@model.get('name')}?",
      "You are about to ignore the error for<br/><b>#{@model.get('path')}</b><br/>You will lose all infomation about this file if you continue with this action.<br/>This will permanently delete the file.",
      () -> @delete(event)
    )

  delete: (event) ->
    @showInProgress(event)
    $.ajax
      url: "#{@url}?path=#{encodeURIComponent(@model.get('path'))}"
      type: 'DELETE'
      success: =>
        @remove()
        @collection.remove(@model)
      error: (err) =>
        @showInError(event)
        console.error('error', err)

  expand: (event) ->
    @$(event.currentTarget)
      .parent()
      .toggleClass('is-collapsed')

  moveDatastore: (event) ->
    @showInProgress(event)
    $.ajax
      url: "#{@url}/move-datastore?path=#{encodeURIComponent(@model.get('path'))}"
      type: 'POST'
      success: =>
        @remove()
        @collection.remove(@model)
        @datastore.add(@model.copy('/eidchub/'))
      error: (err) =>
        @showInError(event)
        console.error('error', err)

  moveMetadata: (event) ->
    @showInProgress(event)
    $.ajax
      url: "#{@url}/move-metadata?path=#{encodeURIComponent(@model.get('path'))}"
      type: 'POST'
      success: =>
        @remove()
        @collection.remove(@model)
        @metadata.add(@model.copy('/supporting-documents/'))
      error: (err) =>
        @showInError(event)
        console.error('error', err)

  render: ->
    @$el.html(@template(@model.attributes))
    @

  showInProgress: (event) ->
    $el = @$(event.currentTarget)
    $el.attr('disabled', true)
    $icon = $el.find('i')
    current = $icon.attr('class')
    $icon.attr('class', 'btn-icon fas fa-sync fa-spin')
    current

  showNormal: (event, classes) ->
    $el = @$(event.currentTarget)
    $el.attr('disabled', false)
    $el.find('i')
      .attr('class', classes)

  showInError: (event) ->
    console.log('In Error')
    $el = @$(event.currentTarget)
    console.log($el)
    $el.attr('disabled', true)
    $el.find('i')
      .attr('class', 'btn-icon fa fa-exclamation-triangle')

  validate: (event) ->
    currentClasses = @showInProgress(event)
    $.ajax
      url: "#{@url}/validate?path=#{encodeURIComponent(@model.get('path'))}"
      type: 'POST'
      success: =>
        setTimeout(
          () =>
            @getServerState(() -> @showNormal(event, currentClasses))
          ,
          5000
        )
      error: (err) =>
        @showInError(event)
        console.error('error', err)
