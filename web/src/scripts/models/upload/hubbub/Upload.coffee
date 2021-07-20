define [
  'backbone'
  'jquery'
], (Backbone, $) -> Backbone.Model.extend

  urlRoot:
    "/upload"

  page:
    documentsPage: 1
    datastorePage: 1
    supportingDocumentsPage: 1

  defaults:
    cancel: no
    message: off




  keyToName:
    documents: 'data'
    'supporting-documents': 'metadata'
    datastore: 'datastore'





  errorType:
    CHANGED_HASH: 'hash'
    NO_HASH: 'hash'
    CHANGED_MTIME: 'file'
    UNKNOWN: 'file'
    UNKNOWN_MISSING: 'file'
    MISSING: 'file'
    MISSING_UNKNOWN: 'file'
    MOVED_UNKNOWN: 'file'
    MOVED_UNKNOWN_MISSING: 'file'
    REMOVED_UNKNOWN: 'file'
    ZIPPED_UNKNOWN: 'file'
    ZIPPED_UNKNOWN_MISSING: 'file'
    INVALID: 'file'

  errorActions:
    CHANGED_HASH: 'accept'
    MOVING_FROM_ERROR: 'accept'
    MOVING_TO_ERROR: 'ignore'
    NO_HASH: 'validate'
    CHANGED_MTIME: 'validate'
    UNKNOWN: 'accept'
    UNKNOWN_MISSING: 'ignore'
    MISSING: 'ignore'
    MISSING_UNKNOWN: 'accept'
    MOVED_UNKNOWN: 'accept'
    MOVED_UNKNOWN_MISSING: 'ignore'
    VALIDATING_HASH: 'validate'
    REMOVED_UNKNOWN: 'accept'
    ZIPPED_UNKNOWN: 'accept'
    ZIPPED_UNKNOWN_MISSING: 'ignore'

  open: {}


  showDelete: (filename) ->
    @modalAction = => @delete(filename)
    fileSplit = filename.split('/')
    file = fileSplit[fileSplit.length - 1]

    @modalData.title = "Delete #{file}?"
    @modalData.body = "This will permanently delete the file <br /><b>#{filename}</b>"
    @set 'modal', 'delete'

  showCancel: (filename) ->
    @modalAction = => @cancel(filename)
    fileSplit = filename.split('/')
    file = fileSplit[fileSplit.length - 1]
    @modalData.title = "Cancel moving #{file}?"
    @modalData.body = 'This will not stop the file from being moved.<br />Only do this if you feel the file is no longer moving to the desired destination, e.g. due to a server error.'
    @set 'modal', 'cancel'

  showIgnore: (filename) ->
    @modalAction = => @ignore(filename)
    fileSplit = filename.split('/')
    file = fileSplit[fileSplit.length - 1]
    @modalData.title = "Ignore the error for #{file}?"
    @modalData.body = "You are about to ignore the error for<br /><b>#{filename}</b><br />You will lose all infomation about this file if you continue with this action."
    @set 'modal', 'ignore'

  cancel: (file) ->
    @open[file] = false
    url = @url() + '/cancel?path=' + encodeURIComponent(file)
    $.ajax {
      url: url
      type: 'POST'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
  }

  move: (file, to) ->
    @open[file] = false
    url = @url() + '/move-' + to + '?&path=' + encodeURIComponent(file)
    $.ajax {
      url: url
      type: 'POST'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  accept: (file) ->
    url = @url() + '/accept?path=' + encodeURIComponent(file)
    $.ajax {
      url: url
      type: 'POST'
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  delete: (file) ->
    url = @url() + '?path=' + encodeURIComponent(file)
    $.ajax {
      url: url
      type: 'DELETE'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

#    TODO: what uses this?
  ignore: (file) ->
    url = @url() + '/delete-upload-file?filename=' + encodeURIComponent(file)
    $.ajax {
      url: url
      headers:
        'Accept': 'application/vnd.upload-document+json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  validate: (file) ->
    url = @url() + '/validate?path=' + encodeURIComponent(file)
    $.ajax {
      url: url
      type: 'POST'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }