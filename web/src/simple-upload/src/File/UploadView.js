import Backbone from 'backbone'
import Dropzone from 'dropzone'
import File from './File'

export default Backbone.View.extend({

    initialize (options) {
        // Prevent Dropzone auto discovery
        Dropzone.simpleUploadDropzone = false
        // Enable Dropzone CSS styling
        this.$el.addClass('dropzone')

        const dropzone = new Dropzone(this.el)
        dropzone.on('success', function (file) {
            options.messages.add(new Backbone.Model({ message: `Uploaded: ${file.name}`, type: 'info' }))
            options.files.add(new File({ name: file.name }))
        })
        dropzone.on('error', (file, errorMessage) => options.messages.add(new Backbone.Model(errorMessage)))
        dropzone.on('complete', file => dropzone.removeFile(file))
    }
})
