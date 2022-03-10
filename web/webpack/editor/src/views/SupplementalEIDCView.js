/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/SupplementalEIDC.tpl'
], function(ObjectInputView, template) { return ObjectInputView.extend({

  template,

  render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('select.function').val(this.model.get('function'));
    return this;
  }
});
 });
