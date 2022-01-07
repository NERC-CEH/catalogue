/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/editor/ObjectInputView',
  'tpl!templates/service-agreement/Author.tpl'
], function(ObjectInputView, template) { return ObjectInputView.extend({

  template,

  modify(event) {
    ObjectInputView.prototype.modify.call(this, event);
    return this.model.set('role', 'author');
  }
});
 });

