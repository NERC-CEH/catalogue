/* eslint-disable
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/editor/ParentStringView',
  'tpl!templates/editor/MultiStringTextbox.tpl'
], function (ParentStringView, childTemplate) {
  return ParentStringView.extend({

    childTemplate,

    initialize (options) {
      return ParentStringView.prototype.initialize.call(this, options)
    }
  })
})
