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
  'bootstrap',
  'cs!views/upload/hubbub/UploadView',
  'cs!models/upload/hubbub/Upload'
], ($, Backbone, Bootstrap, HubbubUploadView, HubbubUploadModel) => ({
  /*
  Initialize the Hubbub Uploader
  */
  initialize() {
//    TODO: load state of the app from html, state set from Jira state
    const id = $('#document-upload').data('guid');
    const model = new HubbubUploadModel();
    model.set('id', id);
    return new HubbubUploadView({
      el: '#document-upload',
      model
    });
  }
}));