/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/SingleView',
  'tpl!templates/editor/Input.tpl'
], function(_, SingleView, template) { return SingleView.extend({

  template,

  events: {
    'change': 'modify'
  },

  initialize(options) {
    SingleView.prototype.initialize.call(this, options);
    (this.render)();
    return this.listenTo(this.model, `change:${this.data.modelAttribute}`, this.render);
  },

  render() {
    SingleView.prototype.render.apply(this);
    this.$('.dataentry').append(this.template({data: _.extend({}, this.data, {value: this.model.get(this.data.modelAttribute)})}));
    if (this.data.readonly) {
      return this.$(':input').prop('readonly', true);
    }
  },

  modify(event) {
    const $target = $(event.target);
    const name = $target.data('name');
    const value = $target.val();

    if (!value) {
      return this.model.unset(name);
    } else {
      return this.model.set(name, value);
    }
  }
});
 });