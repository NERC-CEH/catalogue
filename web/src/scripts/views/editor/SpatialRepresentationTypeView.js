// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/ParentStringView',
  'tpl!templates/editor/SpatialRepresentationType.tpl'
], function(_, ParentStringView, childTemplate) { return ParentStringView.extend({

  childTemplate,

  render() {
    ParentStringView.prototype.render.apply(this);
    return _.each(this.array, (string, index) => {
      return this.$(`#input${ this.data.modelAttribute}${ index } select`).val(string);
    });
  }
});
 });