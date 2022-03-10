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
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/PointOfContact.tpl'
], function (_, ObjectInputView, template) {
  return ObjectInputView.extend({

    template,

    initialize (options) {
      return ObjectInputView.prototype.initialize.apply(this)
    },

    render () {
      ObjectInputView.prototype.render.apply(this)
      return this
    },

    modify (event) {
      const $target = $(event.target)
      const name = $target.data('name')
      const value = $target.val()

      if (_.contains(['deliveryPoint', 'city', 'administrativeArea', 'country', 'postalCode'], name)) {
        let address = _.clone(this.model.get('address'))
        if (value) {
          address[name] = value
          return this.model.set('address', address)
        } else {
          address = _.omit(address, name)
          return this.model.set('address', address)
        }
      } else {
        if (value) {
          return this.model.set(name, value)
        } else {
          return this.model.unset(name)
        }
      }
    }
  })
})
