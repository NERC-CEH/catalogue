define [
  'backbone'
  'jquery'
  'cs!models/EditorMetadata'
], (Backbone, $, EditorMetadata) -> EditorMetadata.extend
#TODO: no need to extend EditorMetadata just for URL, set urlRoot here
  page:
    documentsPage: 1
    datastorePage: 1
    supportingDocumentsPage: 1

  defaults:
    cancel: no
    message: off
    modal: off

  # calculated using Hubbub on the SAN, 22/05/19, recalculate for better estimates
  timeEstimate:
    1400000: '1s'
    6300000: '2s'
    10000000: '3s'
    12000000: '4s'
    24000000: '5s'
    39000000: '6s'
    50000000: '8s'
    54000000: '9s'
    69000000: '10s'
    79000000: '20s'
    170000000: '30s'
    220000000: '40s'
    250000000: '50s'
    290000000: '1m'
    320000000: '1m10s'
    400000000: '1m20s'
    470000000: '1m30s'
    730000000: '2m'
    800000000: '2m30s'
    1300000000: '5m'
    1600000000: '7m'
    4300000000: '8m'
    4700000000: '10m'
    5000000000: '12m'
    5300000000: '14m'
    12000000000: '45m'
    18000000000: '1h'
    46000000000: '2h'
    65000000000: '2h+'


  keyToName:
    documents: 'data'
    'supporting-documents': 'metadata'
    datastore: 'datastore'

  keyToAction:
    documents: 'move-both'
    'supporting-documents': 'move-datastore'
    datastore: 'move-metadata'

  messages:
    CHANGED_HASH:
      content: 'The file has changed'
    CHANGED_MTIME:
      title: 'Detected a possible file change'
      content: """
               The meta information about this file has changed
               This could be as small as someone opening the file and saving it
               Or it could mean the file contents have changed
               Please "VALIDATE" this file then resolve any new errors
               """
    INVALID:
      content: 'Something went wrong with this file'
    MISSING:
      content: 'This file is missing'
    MISSING_UNKNOWN:
      content: 'This file was missing, but has been added manually'
    MOVED_UNKNOWN:
      content: 'This file was moved, but has been added manually'
    MOVED_UNKNOWN_MISSING:
      content: 'This file was moved, then added manually, now removed manually'
    MOVING_FROM:
      content: 'The file is currently being moved'
    MOVING_FROM_ERROR:
      content: 'The file failed to move'
    MOVING_TO:
      content: 'The file is currently being moved'
    MOVING_TO_ERROR:
      content: 'The file failed to move'
    NO_HASH:
      content: 'This file has not been validated or failed to validate'
    REMOVED_UNKNOWN:
      content: 'This file, previously removed, has now been manually added'
    VALIDATING_HASH:
      content: 'This file is currently being validated, large files take a long time to process'
    UNKNOWN:
      content: 'This is an unknown file'
    UNKNOWN_MISSING:
      content: 'This was an unknown file, but has been removed manually'
    WRITING:
      content: 'The file is currently being written to disk'
    ZIPPED_UNKNOWN:
      content: 'This file was zipped and has been manually added'
    ZIPPED_UNKNOWN_MISSING:
      content: 'This file was zipped, was been manually added, now manually removed'

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

  modal: false

  modalData:
    title: 'title'
    body: 'body'

  showFinish: ->
    @modalAction = @finish
    @modalData.title = 'Have you finished uploading files?'
    @modalData.body = 'You will no longer be able to add, remove or update files.'
    @set 'modal', 'finish'

  showDelete: (filename) ->
    @modalAction = => @delete(filename)
    fileSplit = filename.split('/')
    file = fileSplit[fileSplit.length - 1]

    @modalData.title = "Delete #{file}?"
    @modalData.body = "This will permanently delete the file <br /><b>#{filename}"
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

  hideDialog: ->
    @modalData.title = 'title'
    @modalData.body = 'body'
    @set 'modal', off

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
    url = @url() + '/delete?path=' + encodeURIComponent(file)
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

  moveToDatastore: (cb) ->
    url = @url() + '/move-to-datastore'
    $.ajax {
      url: url
      type: 'POST'
      success: (data) =>
        @set(data)
        cb()
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  validateFiles: (cb) ->
    url = @url() + '/validate'
    $.ajax {
      url: url
      type: 'POST'
      success: (data) =>
        @set(data)
        cb()
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  finish: ->
    url = @url() + '/finish'
    $.ajax {
      url: url
      type: 'POST'
      success: ->
        do window.location.reload
      error: (err) ->
        console.error('error', err)
    }

  reschedule: ->
    url = @url() + '/reschedule'
    $.ajax {
      url: url
      type: 'POST'
      success: ->
        do window.location.reload
      error: (err) ->
        console.error('error', err)
    }

  schedule: ->
    url = @url() + '/schedule'
    $.ajax {
      url: url
      type: 'POST'
      success: ->
        do window.location.reload
      error: (err) ->
        console.error('error', err)
    }