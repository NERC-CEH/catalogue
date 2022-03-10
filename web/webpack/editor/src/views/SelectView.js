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
  'underscore',
  'cs!views/editor/InputView',
  'tpl!templates/editor/Select.tpl'
], function (_, InputView, template) {
  return InputView.extend({

    template,

    optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>'),

    initialize (options) {
      this.options = options.options
      return InputView.prototype.initialize.call(this, options)
    },

    render () {
      InputView.prototype.render.apply(this)
      const $select = this.$('select')
      this.options.forEach(option => {
        return $select.append(this.optionTemplate(option))
      })
      $select.val(this.model.get(this.data.modelAttribute))
      return this
    }
  })
})
