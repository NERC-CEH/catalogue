/* eslint-disable
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'dropzone',
  'cs!models/upload/simple/File'
], function (Backbone, Dropzone, File) {
  return Backbone.View.extend({

    initialize (options) {
    // Prevent Dropzone auto discovery
      Dropzone.simpleUploadDropzone = false
      // Enable Dropzone CSS styling
      this.$el.addClass('dropzone')

      const dropzone = new Dropzone(this.el)
      dropzone.on('success', function (file) {
        options.messages.add(new Backbone.Model({ message: `Uploaded: ${file.name}`, type: 'info' }))
        return options.files.add(new File({ name: file.name }))
      })
      dropzone.on('error', (file, errorMessage) => options.messages.add(new Backbone.Model(errorMessage)))
      return dropzone.on('complete', file => dropzone.removeFile(file))
    }
  })
})
