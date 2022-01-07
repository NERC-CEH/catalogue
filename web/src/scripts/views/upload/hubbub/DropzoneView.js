// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'dropzone',
  'filesize',
  'tpl!templates/upload/hubbub/DropzoneFileRow.tpl'
], function(Backbone, Dropzone, filesize, template) { return Backbone.View.extend({

  initialize(options) {
    const {
      el
    } = options;
    const {
      success
    } = options;
    const {
      url
    } = options;

    return new Dropzone(el, this.dropzoneOptions(url, success));
  },

  dropzoneOptions(url, success) {
    return {
      timeout: -1,
      url,
      maxFilesize: 20 * 1000 * 1000,
      autoQueue: true,
      previewTemplate: template(),
      previewsContainer: '.dropzone-files',
      clickable: '.fileinput-button',
      parallelUploads: 1,
      init() {
        this.on('addedfile', function(file) {
          const $file = $(file.previewElement);
          $file.find('.cancel').click(() => this.removeFile(file));
          return $file.find('.file-size-value').text(`${filesize(file.size)}`);
        });

        this.on('uploadprogress', function(file, progress, bytesSent) {
          const $file = $(file.previewElement);
          if (progress < 100) {
            return $file.find('.file-status').text(`Uploaded ${filesize(bytesSent)}`);
          } else {
            $file.find('.file-status').text('Writing to Disk');
            return $file.find('.cancel').attr('disabled', true);
          }
        });

        this.on('success', success);

        return this.on('error', function(file, error, xhr) {
          let message;
          const $file = $(file.previewElement);
          $file.find('.file-status').text('Error');
          $file.find('.file-name i').attr('class', 'fa fa-exclamation-triangle');
          $file.find('.cancel').attr('disabled', false);
          const errorMessages = {
            0: 'No connection',
            403: 'Unauthorized',
            500: 'Internal Server Error'
          };
          if (xhr) { message = errorMessages[xhr.status] || error.error; }
          return $file.find('.file-message').text(message);
        });
      }
    };
  }
});
 });