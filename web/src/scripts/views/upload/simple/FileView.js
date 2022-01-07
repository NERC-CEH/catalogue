// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'tpl!templates/upload/simple/File.tpl'
], function(Backbone, template) { return Backbone.View.extend({

  tagName: 'li',

  template,

  events: {
    'change input': 'select'
  }, 

  initialize() {
    this.listenTo(this.model, 'sync', this.remove);
    return this.listenTo(this.model, 'change', this.render);
  },

  select() {
    const previous = this.model.get('toDelete');
    return this.model.set('toDelete', !previous);
  },

  render() {
    this.$el.html(this.template(this.model.attributes));
    return this;
  }
});
 });
