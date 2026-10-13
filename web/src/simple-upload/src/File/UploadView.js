import Backbone from 'backbone'
import Dropzone from 'dropzone'
import File from './File'

const storedName = file => file.fullPath || file.name

// Dropzone passes its own errors as strings and server errors as the parsed body: the upload
// controller's { message }, or a problem detail ({ detail }) from the global error handler
function failureMessage (file, error) {
  const reason = typeof error === 'string' ? error : (error && (error.message || error.detail || error.error))
  return reason ? `Failed to upload ${file.name}: ${reason}` : `Failed to upload ${file.name}`
}

export default Backbone.View.extend({

  initialize (options) {
    // Prevent Dropzone auto discovery
    Dropzone.simpleUploadDropzone = false
    // Enable Dropzone CSS styling
    this.$el.addClass('dropzone')

    const dropzone = this.dropzone = new Dropzone(this.el)
    dropzone.on('success', function (file) {
      options.messages.add(new Backbone.Model({ message: `Uploaded: ${file.name}`, type: 'info' }))
      // Listed (and later deleted) by the path it was stored at, not just its name
      options.files.add(new File({ name: storedName(file) }))
    })
    dropzone.on('error', (file, error) => options.messages.add(new Backbone.Model({
      message: failureMessage(file, error),
      type: 'error'
    })))
    dropzone.on('sending', (file, _xhr, data) => {
      // The server requires filename; Dropzone sets fullPath only for files inside a dropped folder
      data.append('filename', storedName(file))
    })
    dropzone.on('complete', file => dropzone.removeFile(file))
  }
})
