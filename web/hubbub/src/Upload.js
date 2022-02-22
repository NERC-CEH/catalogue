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
  'jquery'
], (Backbone, $) => Backbone.Model.extend({

  urlRoot:
    '/upload',

  defaults: {
    datastorePage: 1,
    dropboxPage: 1,
    metadataPage: 1,
    datastoreSize: 20,
    dropboxSize: 20,
    metadataSize: 20
  }
}))
