define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) -> DocumentUploadModel.extend
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
      error: (err) ->
        console.error('error', err)
    }
  
  finish: ->
    @save @attributes,
      url: @url() + '/finish'
      success: -> do window.location.reload
