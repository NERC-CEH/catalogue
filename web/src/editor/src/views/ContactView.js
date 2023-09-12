import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'

import template from '../templates/Contact.tpl'

export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    this.$('select.role').val(this.model.get('role'))
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
        this.model.set('address', address)
      } else {
        address = _.omit(address, name)
        this.model.set('address', address)
      }
    } else {
      if (value) {
        this.model.set(name, value)
      } else {
        this.model.unset(name)
      }
    }
  }
})
