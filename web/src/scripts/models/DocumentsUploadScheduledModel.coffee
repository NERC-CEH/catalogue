define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) -> DocumentUploadModel.extend
  uploaded: (file, res) ->
    # @set
    #   documents: res.documents
    #   plone: res.plone
    #   datastore: res.datastore
    #   message:
    #     message: 'Uploaded <b>' + file.name + '</b>'
    #     type: 'success'
    #     timeout: 3000

  delete: (file) ->
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
