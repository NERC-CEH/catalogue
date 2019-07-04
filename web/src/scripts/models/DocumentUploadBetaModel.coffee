define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) -> DocumentUploadModel.extend
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
    plone: 'metadata'
    datastore: 'datastore'
  
  keyToAction:
    documents: 'move-both'
    plone: 'move-datastore'
    datastore: 'move-metadata'

  messages:
    CHANGED_MTIME:
      title: 'Detected a possible file change'
      content: """
The meta information about this file has changed
This could be as small as someone opening the file and saving it
Or it could mean the file contents have changed
Please "VALIDATE" this file then resolve any new errors
"""
    CHANGED_HASH:
      content: 'The file has changed'
    NO_HASH:
      content: 'This file has not been validated or failed to validate'
    UNKNOWN:
      content: 'This is an unknown file'
    UNKNOWN_MISSING:
      content: 'This was an unknown file, but has been removed manually'
    MISSING:
      content: 'This file is missing'
    MISSING_UNKNOWN:
      content: 'This file was missing, but has been added manually'
    MOVED_UNKNOWN:
      content: 'This file was moved, but has been added manually'
    MOVED_UNKNOWN_MISSING:
      content: 'This file was moved, then added manually, now removed manually'
    VALIDATING_HASH:
      content: 'This file is currently being validated, large files take a long time to process'
    REMOVED_UNKNOWN:
      content: 'This file, previously removed, has now been manually added'
    ZIPPED_UNKNOWN:
      content: 'This file was zipped and has been manually added'
    ZIPPED_UNKNOWN_MISSING:
      content: 'This file was zipped, was been manually added, now manually removed'
    INVLAID:
      content: 'Something went wrong with this file'
    MOVING_FROM:
      content: 'The file is currently being moved'
    MOVING_TO:
      content: 'The file is currently being moved'
    WRITING:
      content: 'The file is currently being written to disk'

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
    INVLAID: 'file'

  errorActions:
    CHANGED_HASH: 'accept'
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

  cancel: (file, to) ->
    @open[file] = false
    url = @url() + '/cancel?filename=' + encodeURIComponent(file)
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
  }

  move: (file, to) ->
    @open[file] = false
    url = @url() + '/move-upload-file?to=' + to + '&filename=' + encodeURIComponent(file)
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  accept: (file) ->
    url = @url() + '/accept-upload-file?path=' + encodeURIComponent(file)
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  delete: (file) ->
    url = @url() + '/delete-upload-file?filename=' + encodeURIComponent(file)
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  ignore: (file) ->
    url = @url() + '/delete-upload-file?filename=' + encodeURIComponent(file)
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }
  
  validate: (file) ->
    url = @url() + '/validate-upload-file?path=' + encodeURIComponent(file)
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }
  
  moveToDatastore: () ->
    url = @url() + '/move-to-datastore'
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }
  
  validateFiles: () ->
    url = @url() + '/validate'
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  zip: ->
    url = @url() + '/zip-upload-files'
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  unzip: ->
    url = @url() + '/unzip-upload-files'
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) =>
        console.error('error', err)
        do @fetch
    }

  finish: ->
    url = @url() + '/finish'
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) ->
        do window.location.reload
      error: (err) ->
        console.error('error', err)
    }