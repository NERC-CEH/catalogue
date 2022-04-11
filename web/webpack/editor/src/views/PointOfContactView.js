import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import template from '../templates/PointOfContact.tpl'
import $ from 'jquery'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = _.template(template)
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
