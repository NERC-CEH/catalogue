define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) -> DocumentUploadModel.extend
  accept: (name, file) ->
    # baseMessage = '<b>' + file + '</b>'
    # @postWithFormData window.location.href + '/accept-invalid/' + name,
    #   'Accepted: ' + baseMessage,
    #   'Could not accept: ' + baseMessage,
    #   file: file

  delete: (name, file) ->
    # baseMessage = '<b>' + file + '</b>'
    # @postWithFormData window.location.href + '/delete/' + name,
    #   'Deleted: ' + baseMessage,
    #   'Could not delete: ' + baseMessage,
    #   file: file

  ignore: (name, file) ->
    console.log 'ignore', name, file
    # baseMessage = '<b>' + file + '</b>'
    # @postWithFormData window.location.href + '/delete/' + name,
    #   'Ignored: ' + baseMessage,
    #   'Could not ignore: ' + baseMessage,
    #   file: file

  move: (file, from, to) ->
    # baseMessage = '<b>' + file + 'from <u>' + from + '</u> to <u>' + to + '</u>'
    # @postWithFormData window.location.href + '/move',
    #   'Moved: ' + baseMessage,
    #   'Could not move: ' + baseMessage,
    #   file: file
    #   from: from
    #   to: to
  
  moveToDatastore: (files) ->
    # baseMessage = '<b>' + files.join(', ') + ' from <u>Documents</u> to <u>Datastore</u>'
    # @postWithFormData window.location.href + '/move-all',
    #   'Moved: ' + baseMessage,
    #   'Could not move: ' + baseMessage,
    #   files: files
    #   from: 'documents'
    #   to: 'datastore'

  zip: ->
    # @save null,
    #   url: window.location.href + '/zip/datastore'
    #   success: (xhr, res) => @success 'Zipped'
    #   error: (xhr, error) => @error error, 'Could not zip'

  unzip: ->
    # @save null,
    #   url: window.location.href + '/unzip/datastore'
    #   success: (xhr, res) => @success 'Unzipped'
    #   error: (xhr, error) => @error error, 'Could not unzip'


    # delete: (file) ->
    # @save @attributes,
    #   url: @url() + '/delete-upload-file?filename=' + encodeURIComponent(file)