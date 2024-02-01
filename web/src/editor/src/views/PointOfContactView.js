import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/PointOfContact'

export default ObjectInputView.extend({

    initialize () {
        this.template = template
        ObjectInputView.prototype.initialize.apply(this)
    },

    render () {
        ObjectInputView.prototype.render.apply(this)
        return this
    },

    modify (event) {
        const name = $(event.target).data('name')
        const value = $(event.target).val()

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
