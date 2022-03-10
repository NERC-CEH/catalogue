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
  'cs!views/editor/ObjectInputView',
  'cs!views/editor/MapStyleSelectorView',
  'tpl!templates/editor/MapBucket.tpl'
], function (ObjectInputView, MapStyleSelectorView, template) {
  return ObjectInputView.extend({

    template,

    initialize (options) {
      ObjectInputView.prototype.initialize.call(this, options)

      return new MapStyleSelectorView({
        el: this.$('.style-selector'),
        model: this.model.getRelated('style'),
        disabled: options.disabled
      })
    }
  })
})
