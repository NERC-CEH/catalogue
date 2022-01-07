/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'tpl!templates/upload/simple/Message.tpl'
], function(Backbone, template) { return Backbone.View.extend({

  tagName: 'li',

  template,

  initialize() {
    return this.listenTo(this.model, 'remove', this.remove);
  },

  render() {
    this.$el.html(this.template(this.model.attributes));
    return this;
  }
});
 });
