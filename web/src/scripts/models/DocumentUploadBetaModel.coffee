define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) -> DocumentUploadModel.extend
  move: (file, to) ->
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