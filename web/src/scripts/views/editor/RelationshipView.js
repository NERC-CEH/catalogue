// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/Relationship.tpl'
], function(_, ObjectInputView, template) { return ObjectInputView.extend({

  template,

  optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>'),

  initialize(options) {
    this.options = options.options;
    return ObjectInputView.prototype.initialize.call(this, options);
  },

  render() {
    ObjectInputView.prototype.render.apply(this);
    const $list = this.$('datalist');
    this.options.forEach(option => {
      return $list.append(this.optionTemplate(option));
    });
    return this;
  }
});
 });