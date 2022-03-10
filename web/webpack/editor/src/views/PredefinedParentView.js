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
  'cs!views/editor/ParentView',
  'tpl!templates/editor/PredefinedParent.tpl',
  'tpl!templates/editor/PredefinedParentDropdown.tpl'
], function (_, ParentView, template, dropdownTemplate) {
  return ParentView.extend({

    events: {
      'click .dropdown-menu': 'setPredefined'
    },

    render () {
      ParentView.prototype.render.apply(this)
      this.$('button.add').replaceWith(template({ data: this.data }))
      this.$('button').prop(this.data.disabled, this.data.disabled)
      const $dropdown = this.$('ul.dropdown-menu')
      _.chain(this.data.predefined)
        .keys()
        .each(item => $dropdown.append(dropdownTemplate({ predefined: item })))
      return this
    },

    setPredefined (event) {
      (event.preventDefault)()
      const value = $(event.target).text()
      let selected = {}

      if (value !== 'Custom') {
        selected = this.data.predefined[value]
      }

      return this.collection.add(selected)
    }
  })
})
