// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/TemporalExtent.tpl'
], function(ObjectInputView, template) { return ObjectInputView.extend({

  template,

  render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('input').datepicker({dateFormat: "yy-mm-dd"});
    return this;
  }
});
 });