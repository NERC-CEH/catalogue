/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/SingleView',
  'tpl!templates/service-agreement/TextOnly.tpl'
], function(_, SingleView, template) { return SingleView.extend({ 

  template,

  className: 'component component--textonly',

  initialize(options) {
    SingleView.prototype.initialize.call(this, options);
    return (this.render)();
  },

  render() {
    SingleView.prototype.render.apply(this);
    return this.$('.dataentry').append(this.template({data: _.extend({}, this.data, {value: this.model.get})}));
  }
});
 });