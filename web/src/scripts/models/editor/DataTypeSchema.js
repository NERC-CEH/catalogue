/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'underscore'
], function(Backbone, _) { return Backbone.Model.extend({

  defaults: {
    constraints: {}
  },

  toJSON() {
    if (_.isEmpty(this.get('constraints'))) {
      return _.omit(this.attributes, 'constraints');
    } else {
      return _.clone(this.attributes);
    }
  }
});
 });