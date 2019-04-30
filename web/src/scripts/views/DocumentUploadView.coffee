define [
  'jquery'
  'backbone'
  'tpl!templates/DocumentUploadFileRow.tpl'
], ($, Backbone, DocumentUploadFileRowTemplate) -> Backbone.View.extend
  keyToName:
    documents: 'documents'
    plone: 'metadata'
    datastore: 'datastore'

  messages:
    CHANGED_HASH: 'The file has changed'
    NO_HASH: 'This file has not been succesfully validated'
    CHANGED_MTIME: 'The meta information about this file has changed'
    UNKNOWN: 'This is an unknown file'
    UNKNOWN_MISSING: 'This was an unknown file, but has been removed manually'
    MISSING: 'This file is missing'
    MISSING_UNKNOWN: 'This file was missing, but has been added manually'
    MOVED_UNKNOWN: 'This file was moved, but has been added manually'
    MOVED_UNKNOWN_MISSING: 'This file was moved, then added manually, now removed manually'
    VALIDATING_HASH: 'This file is currently being validated, large files take a long time to process'
    REMOVED_UNKNOWN: 'This file, previously removed, has now been manually added'
    ZIPPED_UNKNOWN: 'This file was zipped and has been manually added'
    ZIPPED_UNKNOWN_MISSING: 'This file was zipped, was been manually added, now manually removed'
    INVLAID: 'Something went wrong with this file'

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

  initialize: ->
    setInterval(
      () => do @model.fetch
      10000
    )

    @model.on 'sync', =>
      do @render
      do $('.loading').remove
      $('.messages').hide 'fast'
    do @model.fetch

  render: ->
    uploadFiles = @model.get('uploadFiles')

    for name of uploadFiles
        filesEl = $(".#{name}-files")
        filesEl.html('')
        if typeof uploadFiles[name].documents == 'undefined' || uploadFiles[name].invalid == 'undefined'
            filesEl.append($("<h3 class='no-documents'>NO FILES IN #{@keyToName[name].toUpperCase()}</h3>"))
        for filename, data of uploadFiles[name].invalid
            data.message = @messages[data.type]
            data.errorType = @errorType[data.type] || ''
            data.hash = data.hash || 'NO HASH'
            row = $(DocumentUploadFileRowTemplate data)
            filesEl.append(row)
        for filename, data of uploadFiles[name].documents
            data.errorType = 'valid'
            row = $(DocumentUploadFileRowTemplate data)
            filesEl.append(row)
