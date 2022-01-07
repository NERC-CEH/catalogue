// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!models/editor/Contact'
], (_, Contact) => Contact.extend({

  defaults: _.extend({}, Contact.prototype.defaults, {role: 'pointOfContact'})}));

({
  intialize() {
    return Contact.prototype.initialize.apply(this);
  }
});
