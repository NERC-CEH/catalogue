/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone',
  'tpl!templates/editor/Child.tpl'
], function(_, Backbone, template) { return Backbone.View.extend({

  className: 'row',

  events: {
    'click button.remove': 'delete'
  },

  initialize(options) {
    this.data = options;
    this.listenTo(this.model, 'remove', function() { return (this.remove)(); });
    this.index = this.model.collection.indexOf(this.model);
    (this.render)();
    return new this.data.ObjectInputView(_.extend({}, this.data, {
      el: this.$('.dataentry'),
      model: this.model,
      index: this.index
    }
    )
    );
  },

  render() {
    this.$el.html(template({index: this.index, data: this.data}));
    return this;
  },

  delete() {
    return this.model.collection.remove(this.model);
  }
});
 });
