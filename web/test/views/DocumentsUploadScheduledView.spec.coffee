define [
  'jquery'
  'cs!views/DocumentsUploadScheduledView'
  'backbone'
], ($, DocumentsUploadScheduledView, Backbone) ->
  describe 'Documents Upload Scheduled View', ->
    dropzoneEnabled = null

    model = null
    view = null

    class Dropzone
      constructor: (selector, obj) ->
        obj.init.apply @
      on: (event, callback) ->
      disable: ->
        dropzoneEnabled = no
    
    window.Dropzone = Dropzone

    beforeEach ->
      root = document.createElement 'div'
      root.id = 'root'
      $('#root').remove()
      $(document.body).append root
      $('#root').html '''
         <div class='loading' />
         <div class='messages' style='height: 100px;' />
         <button class='finish' disabled />
         <div class='files' />
         <div class='file uploading-file-txt' />
         <div class='empty-message'>init no files</div>
         <div class='modal-title' />
         <div class='modal-body' />
         <button class='modal-dismiss' />
         <button class='modal-accept' />
      '''

      model = new Backbone.Model()
      model.set 'documents',
        files: [{
          id: 'file-txt',
          name: 'file.txt'
        }]

      model.finish = jasmine.createSpy('finish')
      model.delete = jasmine.createSpy('delete')

      view = new DocumentsUploadScheduledView
        model: model
      do view.render

    it 'removes loading on init', ->
      expect($('.loading').length).toBe 0
      expect($('.finish').attr 'disabled').toBe undefined

    it 'enables finish which will open modal dialog which will finish on accept', ->
      do $('.finish').click
      
      title = $('.modal-title').html()
      body = $('.modal-body').html()
      dismiss = $('.modal-dismiss').html()
      accept = $('.modal-accept').html()

      expect(title).toBe 'Finish'
      expect(body).toBe 'You will no longer be able to add, remove or update files! ARE YOU SURE?'
      expect(dismiss).toBe 'No'
      expect(accept).toBe 'Yes'
      do $('.modal-accept').click

      expect(dropzoneEnabled).toBe no
      expect(model.finish).toHaveBeenCalled()
      expect($('.finish').attr 'disabled').toBe 'disabled'
      expect($('.file').attr 'disabled').toBe 'disabled'
      expect($('.message').length).not.toBe 0

    it 'has no empty message if there are files', ->
      expect($('.empty-message').html()).toBe ''
    
    it 'renders files, removes uploading files if in data', ->
      expect($('#documents-file-txt').length).toBe 1
      expect($('.uploading-file-txt').length).toBe 0
    
    it 'shows modal when delete selected', ->
      do $('.delete').click

      title = $('.modal-title').html()
      body = $('.modal-body').html()
      dismiss = $('.modal-dismiss').html()
      accept = $('.modal-accept').html()

      expect(title).toBe 'Delete <b>file.txt</b>'
      expect(body).toBe 'Are you sure you want to permanently delete file.txt'
      expect(dismiss).toBe 'No'
      expect(accept).toBe 'Yes'
      do $('.modal-accept').click

      expect(model.delete).toHaveBeenCalled()

      