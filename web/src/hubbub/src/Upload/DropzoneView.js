import $ from 'jquery'
import Backbone from 'backbone'
import Dropzone from 'dropzone'
import { filesize } from 'filesize'
import template from './previewTemplate'

export default Backbone.View.extend({

  initialize (options) {
    const { el, success, url, complete } = options
    // The server's limit, in bytes, from the page. Dropzone's maxFilesize is in MiB; refusing an
    // oversized file here saves uploading gigabytes only to be rejected (dri-one #459)
    const maxBytes = Number($(el).data('max-file-size'))
    this.dropzone = new Dropzone(el, this.dropzoneOptions(url, success, complete, maxBytes))
  },

  dropzoneOptions (url, success, complete, maxBytes) {
    return {
      timeout: -1,
      url,
      maxFilesize: maxBytes > 0 ? maxBytes / (1024 * 1024) : 2048,
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

        this.on('queuecomplete', complete)

        this.on('sending', (file, _xhr, data) => {
          if (file.fullPath) {
            data.append('filename', file.fullPath)
          } else {
            data.append('filename', file.name)
          }
        })

        this.on('error', function (file, error, xhr) {
          const $file = $(file.previewElement)
          $file.find('.file-status').text('Error')
          $file.find('.file-name i').attr('class', 'fa fa-exclamation-triangle')
          $file.find('.cancel').attr('disabled', false)
          const errorMessages = {
            0: 'No connection',
            403: 'Unauthorized',
            500: 'Internal Server Error'
          }
          // Dropzone's own errors (too big, cancelled) are strings with no xhr; the server sends a
          // string, or a problem detail with the reason in detail
          const reason = typeof error === 'string' ? error : (error && (error.detail || error.message || error.error))
          $file.find('.file-message').text((xhr && errorMessages[xhr.status]) || reason || 'Upload failed')
        })
      }
    }
  }
})
