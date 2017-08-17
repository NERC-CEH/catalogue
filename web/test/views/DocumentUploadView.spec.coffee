define [
  'jquery'
  'cs!views/DocumentsUploadView'
  'dropzone'
], ($, DocumentsUploadView) ->
  describe 'DocumentsUploadView', ->
    el = null
    view = null

    createFile = (id, size) ->
        id: id
        status: ''
        type: ''
        size: size || 0

    beforeEach ->
        do jasmine.clock().install

        el = $("""
        <div id='delete' />
        <div class="checksums-list" />

        <a class="fileinput-button" disabled />
        <a class="upload-all" disabled />
        <a class="cancel-all" disabled />

        <div class="dz">
            <span class='title' />
            <div id="previews" />
        </div>
        """)
        .appendTo $('body')
        
        view = new DocumentsUploadView

    afterEach ->
        do el.remove
        do jasmine.clock().uninstall

    describe 'once dropzone is loaded', ->
        it 'changes title to "Drag files"', ->
            text = $('.dz .title').text()
            expect(text).toBe 'Drag files here'

        it 'enables the fileinput button', ->
            disabled = $('.fileinput-button').attr 'disabled'
            do expect(disabled).not.toBeDefined
    
    describe 'adding a file', ->
        beforeEach ->
            view.dropzone.addFile createFile('file-id')

        it 'enables Upload All', ->
            disabled = $('.upload-all').attr 'disabled'
            do expect(disabled).not.toBeDefined

        it 'enables Cancel All', ->
            disabled = $('.cancel-all').attr 'disabled'
            do expect(disabled).not.toBeDefined
        
        it 'adds an upload button which will enqueue the file', ->
            enqueuedFile = null
            view.dropzone.enqueueFile = (file) -> enqueuedFile = file

            do $('#file-row-0 .upload').click

            expect(enqueuedFile.id).toBe  'file-row-0'

        it 'adds a cancel button which will remove the file', ->
            removedFile = null
            view.dropzone.removeFile = (file) -> removedFile = file

            do $('#file-row-0 .cancel').click

            expect(removedFile.id).toBe  'file-row-0'

    describe 'max size message', ->
        it 'is displayed for large files', ->
            file = createFile('file-id', 1000 + 1)
            view.dropzone.addFile file

            className = $('#file-row-0 .max-size').attr 'class'
            expect(className).toContain 'is-active'
            expect(className).not.toContain 'is-inactive'

        it 'is not displayed for small files', ->
            file = createFile('file-id', 1000)
            view.dropzone.addFile file

            className = $('#file-row-0 .max-size').attr 'class'
            expect(className).toContain 'is-inactive'
            expect(className).not.toContain 'is-active'

    it 'disables the Upload All and Cancel All buttons when all files have been removed', ->
        file = createFile('file-id')
        view.dropzone.addFile file
        view.dropzone.removeFile file

        disabled = $('.upload-all').attr 'disabled'
        expect(disabled).toBe 'disabled'

        disabled = $('.cancel-all').attr 'disabled'
        expect(disabled).toBe 'disabled'
    
    it 'does not disable the Upload All and Cancel All buttons if any files remane after removal', ->
        file1 = createFile('file-id-1')
        file2 = createFile('file-id-2')
        view.dropzone.addFile file1
        view.dropzone.addFile file2
        view.dropzone.removeFile file1

        disabled = $('.upload-all').attr 'disabled'
        do expect(disabled).not.toBeDefined

        disabled = $('.cancel-all').attr 'disabled'
        do expect(disabled).not.toBeDefined
    
    describe 'errors', ->
        emitError = (status) ->
            file = createFile('file-id')
            view.dropzone.addFile file
            view.dropzone.emit 'error',
                file,
                'Unused error message',
                status: status
        
        it 'makes the progress bar full width', ->
            emitError 9001
            style = $('#file-row-0 .progress-bar').attr('style')
            expect(style).toBe 'width: 100%;'

        it 'changes progress bar to danger', ->
            emitError 9001
            className = $('#file-row-0 .progress-bar').attr('class')
            expect(className).not.toContain 'progress-bar-success'
            expect(className).toContain 'progress-bar-danger'

        it 'defualts to "Failed"', ->
            emitError 9001
            text = $('#file-row-0 .progress-bar').text()
            expect(text).toBe 'Failed'
        
        it 'is "Already exists" when conflict (409)', ->
            emitError 409
            text = $('#file-row-0 .progress-bar').text()
            expect(text).toBe 'Already exists'

        it 'is "Unauthorized" when user is not allowed to upload (403)', ->
            emitError 403
            text = $('#file-row-0 .progress-bar').text()
            expect(text).toBe 'Unauthorized'


    it '"Upload All" will enqueue all the files', ->
        file1 = createFile('file-id-1')
        view.dropzone.addFile file1
        file2 = createFile('file-id-2')
        view.dropzone.addFile file2
        uploadAllFiles = null
        view.dropzone.enqueueFiles = (files) -> uploadAllFiles = files

        do $('.upload-all').click

        expect(uploadAllFiles).toEqual [file1, file2]
    
    it '"Cancel All" will remove all the files and cancel the upload', ->
        file1 = createFile('file-id-1')
        view.dropzone.addFile file1
        file2 = createFile('file-id-2')
        view.dropzone.addFile file2
        removedAllFilesAndCancelUpload = null
        view.dropzone.removeAllFiles = (cancelUpload) -> removedAllFilesAndCancelUpload = cancelUpload

        do $('.cancel-all').click

        expect(removedAllFilesAndCancelUpload).toBe yes
    
    it '"Cancel All" disbales the "Upload All" and "Cancel All" buttons', ->
        file = createFile('file-id')
        view.dropzone.addFile file

        do $('.cancel-all').click

        disabled = $('.upload-all').attr 'disabled'
        expect(disabled).toBe 'disabled'

        disabled = $('.cancel-all').attr 'disabled'
        expect(disabled).toBe 'disabled'
        

    describe 'on successful upload', ->
        file = null

        emitSuccess = ->
            view.dropzone.emit 'success', file, [{
                filename: file.filename
                md5Hash: file.md5Hash
            }]

        beforeEach ->
            file = createFile('file-id')
            view.dropzone.addFile file
            do emitSuccess
        
        it 'updates progress bar with "Uploaded"', ->
            text = $('#file-row-0 .progress-bar').text()
            expect(text).toBe 'Uploaded'

        describe 'after half a second', ->
            beforeEach ->
                jasmine.clock().tick(500)

            it 'updates the checksums', ->
                do expect($('.checksums-list').html()).toBeDefined
                expect($('.checksums-list').html()).not.toBe('')
            
            it 'removes the file', ->
                expect(view.dropzone.files.length).toBe 0
    
    describe 'deleting a file', ->
        fileName = 'filename'
        event = null
        beforeEach ->
            ajax = spyOn($, "ajax")
            view.dropzone.emit 'success', {}, [{
                filename: 'filename'
            }]
            jasmine.clock().tick 500
            do $('.delete').click
            event = ajax.calls.mostRecent().args[0]

        it 'creates http DELETE with the file name', ->
            expect(event.url).toBe window.location.href + '/' + fileName
            expect(event.type).toBe 'DELETE'