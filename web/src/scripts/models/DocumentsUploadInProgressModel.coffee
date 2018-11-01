define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) -> DocumentUploadModel.extend
  accept: (name, file) ->
    @save @attributes,
      url: @url() + '/accept-upload-file?name=' + name + '&filename=' + encodeURIComponent(file)
      error: -> do window.location.href = window.location.href + '/validate'

  delete: (name, file) ->
    @save @attributes,
      url: @url() + '/delete-upload-file?name=' + name + '&filename=' + encodeURIComponent(file)
      error: -> do window.location.href = window.location.href + '/validate'

  ignore: (name, file) ->
    @save @attributes,
      url: @url() + '/delete-upload-file?name=' + name + '&filename=' + encodeURIComponent(file)
      error: -> do window.location.href = window.location.href + '/validate'

  move: (file, from, to) ->
    @save @attributes,
      url: @url() + '/move-upload-file?from=' + from + '&to=' + to + '&filename=' + encodeURIComponent(file)
      error: -> do window.location.href = window.location.href + '/validate'

  moveToDatastore: (files) ->
    @save @attributes,
      url: @url() + '/move-to-datastore'
      error: -> do window.location.href = window.location.href + '/validate'

  zip: ->
    @save @attributes,
      url: @url() + '/zip-upload-files'
      error: -> do window.location.href = window.location.href + '/validate'

  unzip: ->
    @save @attributes,
      url: @url() + '/unzip-upload-files'
      error: -> do window.location.href = window.location.href + '/validate'
