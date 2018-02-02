define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) -> DocumentUploadModel.extend
  delete: (file) ->
    @save @attributes, {
      url: @url() + '/delete-upload-file?filename=' + encodeURIComponent(file)
    }
    # baseMessage = '<b>' + file + '</b>'
    # @postWithFormData window.location.href + '/delete/documents',
    #   'Deleted: ' + baseMessage,
    #   'Could not delete: ' + baseMessage,
    #   file: file
  
  finish: ->
    # @save null,
      # url: window.location.href + '/finish'
      # success: (res) -> do window.location.reload
      # error: (xhr, error) => @error error, 'Could not finish'
