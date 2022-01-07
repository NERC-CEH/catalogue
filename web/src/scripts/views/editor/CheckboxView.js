/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/editor/InputView',
  'tpl!templates/editor/Checkbox.tpl'
], function(InputView, template) { return InputView.extend({

  template,

  render() {
    InputView.prototype.render.apply(this);
    this.$('[type="checkbox"]').prop('checked', this.model.get(this.data.modelAttribute));
    return this;
  },

  modify(event) {
    const $target = $(event.target);
    const name = $target.data('name');
    const value = $target.prop('checked');
    return this.model.set(name, value);
  }
});
 });