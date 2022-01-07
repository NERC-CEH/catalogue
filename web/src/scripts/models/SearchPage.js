// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone'
], function(_, Backbone) { return Backbone.Model.extend({

  url() { return window.location.pathname; },

  defaults: {
    results:  [],
    prevPage: null,
    nextPage: null
  },

  /*
  This SearchPage may have the selected id populated. If it does, this method
  will return the full result which is selected. If nothing is selected, return
  undefined.
  */
  getSelectedResult() {
    if (this.has('results')) {
      return _.find(this.attributes.results, e => e.identifier === this.attributes.selected);
    }
  }
});
 });
