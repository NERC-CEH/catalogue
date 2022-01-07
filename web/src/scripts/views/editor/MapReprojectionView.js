/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/MapReprojection.tpl'
], function(_, ObjectInputView, template) { return ObjectInputView.extend({

  template,

  events: _.extend({}, ObjectInputView.prototype.events,
    {'click button.remove': 'delete'}),

  delete() { return this.model.collection.remove(this.model); }
});
 });