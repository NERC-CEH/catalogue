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
  'tpl!templates/editor/InfrastructureChallenge.tpl'
], function (ObjectInputView, template) {
  return ObjectInputView.extend({

    template,

    render () {
      ObjectInputView.prototype.render.apply(this)
      this.$('select').val(this.model.get('value'))
      return this
    }
  })
})
