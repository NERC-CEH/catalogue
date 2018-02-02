define [
  'cs!models/DocumentUploadModel'
], (DocumentUploadModel) ->
    describe 'thingy', ->
        it 'works', ->

#   documentUploadModel = null
#   saveArgs = null

#   beforeEach ->
#     documentUploadModel = new DocumentUploadModel()
#     save = spyOn documentUploadModel, 'save'
#     documentUploadModel.postWithFormData "url", "successMessage", "errorMessage",
#         a: 1
#     saveArgs = save.calls.mostRecent().args[1]

#   describe 'DocumentUploadModel', ->

#     it 'postWithFormData calls url and data is form data', ->
#         expect(saveArgs.url).toEqual 'url'
#         expect((saveArgs.data).toString()).toEqual '[object FormData]'
#         expect(saveArgs.processData).toBe false
#         expect(saveArgs.contentType).toBe false

#     it 'sets success message on success', ->
#         do saveArgs.success
#         message = documentUploadModel.get 'message'
#         expect(message).toEqual
#             message: 'successMessage'
#             type: 'success'
#             timeout: 3000
    
#     it 'cancels on error', ->
#         do saveArgs.error
#         cancel = documentUploadModel.get 'cancel'
#         expect(cancel).toBe yes

#     it 'sets error message on error', ->
#         do saveArgs.error
#         message = documentUploadModel.get 'message'
#         expect(message).toEqual
#             message: 'errorMessage'
#             type: 'warning'
#             timeout: 3000

#     it 'sets error message on error with response', ->
#         saveArgs.error null, { responseText: 'reason' }
#         message = documentUploadModel.get 'message'
#         expect(message).toEqual
#             message: 'errorMessage because reason'
#             type: 'warning'
#             timeout: 3000
