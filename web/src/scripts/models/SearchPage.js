/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */

import 'backbone'
import 'underscore'
  
export default Backbone.Model.extend({

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
      _.find(this.attributes.results, e => e.identifier === this.attributes.selected);
    }
  }
});
