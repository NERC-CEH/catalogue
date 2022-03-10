/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/ModelQA.tpl'
], function(ObjectInputView, template) { return ObjectInputView.extend({

  template,


  render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('select.category').val(this.model.get('category'));
    return this;
  }
});
 });
 