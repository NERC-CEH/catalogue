define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) -> DocumentUploadModel.extend
  delete: (file) ->
    @save @attributes,
      url: @url() + '/delete-upload-file?filename=' + encodeURIComponent(file)
  
  finish: ->
    @save @attributes,
      url: @url() + '/finish'
      success: -> do window.location.reload
