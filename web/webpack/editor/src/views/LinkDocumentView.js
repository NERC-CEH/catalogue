/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'tpl!templates/editor/LinkDocument.tpl'
],
function(Backbone, template) { return Backbone.View.extend({

  events: {
    'click button': 'selected'
  },

  render() {
    this.$el.html(template(this.model.attributes));
    return this;
  },

  selected(event) {
    return this.model.trigger('selected', this.model.get('identifier'));
  }
});
 });