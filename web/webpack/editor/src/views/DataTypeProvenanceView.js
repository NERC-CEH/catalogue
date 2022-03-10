/* eslint-disable
    no-new,
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
  'tpl!templates/editor/DataTypeProvenance.tpl',
  'cs!views/editor/ParentStringView'
], function (ObjectInputView, template, ParentStringView) {
  return ObjectInputView.extend({

    template,

    render () {
      ObjectInputView.prototype.render.apply(this)
      this.$('input').datepicker({ dateFormat: 'yy-mm-dd' })

      new ParentStringView({
        el: this.$('#provenanceContributors'),
        model: this.model,
        modelAttribute: 'contributors',
        label: 'Contributors'
      })
      return this
    }
  })
})

// # LOOK HERE!
