// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'backbone',
  'cs!collections/upload/simple/FileCollection',
  'cs!views/upload/simple/FileListView',
  'cs!views/upload/simple/MessageListView',
  'cs!views/upload/simple/UploadView'
], ($, Backbone, FileCollection, FileListView, MessageListView, UploadView) => Backbone.View.extend({

  initialize(options) {
    const files = new FileCollection({
      url: options.url});

    const messages = new Backbone.Collection();

    const messageListView = new MessageListView({
      el: '#messages',
      messages
    });

    const uploadView = new UploadView({
      el: '#simple-upload-dropzone',
      files,
      messages,
      url: options.url
    });

    const fileListView = new FileListView({
      el: '#files',
      files,
      messages
    });

    const $filesData = $('#files-data');
    if ($filesData.length) { files.reset(JSON.parse($filesData.text())); }
    const $messageData = $('#messages-data');
    if ($messageData.length) { return messages.reset(JSON.parse($messageData.text())); }
  }
}));
