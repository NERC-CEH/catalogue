define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) -> DocumentUploadModel.extend
  accept: (name, file) ->
    url = @url() + '/accept-upload-file?path=' + encodeURIComponent(file)
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) ->
        console.error('error', err)
    }

  delete: (name, file) ->
    url = @url() + '/delete-upload-file?filename=' + encodeURIComponent(file)
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) ->
        console.error('error', err)
    }

  ignore: (name, file) ->
    url = @url() + '/delete-upload-file?filename=' + encodeURIComponent(file)
    $.ajax {
      url: url
      headers:
        'Accept': 'application/json'
        'Content-Type': 'application/vnd.upload-document+json'
      type: 'PUT'
      success: (data) =>
        @set(data)
      error: (err) ->
        console.error('error', err)
    }

  move: (file, from, to) ->
    @save @attributes,
      url: @url() + '/move-upload-file?from=' + from + '&to=' + to + '&filename=' + encodeURIComponent(file)

  moveToDatastore: (files) ->
    @save @attributes,
      url: @url() + '/move-to-datastore'

  zip: ->
    @save @attributes,
      url: @url() + '/zip-upload-files'

  unzip: ->
    @save @attributes,
      url: @url() + '/unzip-upload-files'
