import $ from 'jquery'
import Backbone from 'backbone'
import Dropzone from 'dropzone'
import { filesize } from 'filesize'
import template from './previewTemplate'

export default Backbone.View.extend({

  initialize (options) {
    const { el, success, url } = options
    // eslint-disable-next-line no-new
    new Dropzone(el, this.dropzoneOptions(url, success))
  },

  dropzoneOptions (url, success) {
    return {
      timeout: -1,
      url,
      maxFilesize: 20 * 1000 * 1000,
      previewTemplate: template,
      previewsContainer: '.dropzone-files',
      clickable: '.fileinput-button',
      init () {
        this.on('addedfile', function (file) {
          const $file = $(file.previewElement)
          $file.find('.cancel').click(() => this.removeFile(file))
          return $file.find('.file-size-value').text(`${filesize(file.size)}`)
        })

        this.on('uploadprogress', function (file, progress, bytesSent) {
          const $file = $(file.previewElement)
          if (progress < 100) {
            return $file.find('.file-status').text(`Uploaded ${filesize(bytesSent)}`)
          } else {
            $file.find('.file-status').text('Writing to Disk')
            return $file.find('.cancel').attr('disabled', true)
          }
        })

        this.on('success', success)

        this.on('error', function (file, error, xhr) {
          let message
          const $file = $(file.previewElement)
          $file.find('.file-status').text('Error')
          $file.find('.file-name i').attr('class', 'fa fa-exclamation-triangle')
          $file.find('.cancel').attr('disabled', false)
          const errorMessages = {
            0: 'No connection',
            403: 'Unauthorized',
            500: 'Internal Server Error'
          }
          if (xhr) { message = errorMessages[xhr.status] || error.error }
          $file.find('.file-message').text(message)
        })
      }
    }
  }
})
