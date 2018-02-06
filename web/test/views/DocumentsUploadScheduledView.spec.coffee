define [
  'jquery'
  'cs!views/DocumentsUploadScheduledView'
  'backbone'
], ($, DocumentsUploadScheduledView, Backbone) ->
  describe 'Documents Upload Scheduled View', ->
    dropzoneEnabled = null

    model = null
    view = null
    data = null

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

      data =
        uploadFiles:
          documents:
            documents:
              filename:
                id: 'file-txt'
                name: 'file.txt'
                path: 'file.txt'
      callbacks = {}
      Model = ->
      Model.prototype.url = -> '/'
      Model.prototype.on = (name, callback) ->
        callbacks[name] = callback
      Model.prototype.fetch = ->
        do callbacks.sync
        do callbacks.change
      Model.prototype.get = (key) ->
        data[key]
      Model.prototype.set = (parentKey, values) ->
        if (typeof values == 'undefined')
          for key, value of parentKey
            data[key] = value
        else
          for key, value of parentKey
            data[parentKey] = values
      Model.prototype.finish = jasmine.createSpy('finish')
      Model.prototype.delete = jasmine.createSpy('delete')
      Model.prototype.bind = -> @

      model = new Model()

      view = new DocumentsUploadScheduledView
        model: model
      do view.render

    it 'removes loading on init', ->
      expect($('.loading').length).toBe 0
      expect($('.finish').attr 'disabled').toBe undefined

    it 'enables finish which will open modal dialog which will finish on accept', ->
      do $('.finish').click
      expect(data.modal.title).toBe('Finish')
      expect(data.modal.body).toBe('You will no longer be able to add, remove or update files! ARE YOU SURE?')
      expect(data.modal.dismiss).toBe('No')
      expect(data.modal.accept).toBe('Yes')

    it 'has no empty message if there are files', ->
      expect($('.empty-message').html()).toBe ''
    
    it 'renders files, removes uploading files if in data', ->
      expect($('#documents-file-txt').length).toBe 1
      expect($('.uploading-file-txt').length).toBe 0
    
    it 'shows modal when delete selected', ->
      do $('.delete').click

      expect(data.modal.title).toBe 'Delete <b>file.txt</b>'
      expect(data.modal.body).toBe 'Are you sure you want to permanently delete file.txt'
      expect(data.modal.dismiss).toBe 'No'
      expect(data.modal.accept).toBe 'Yes'