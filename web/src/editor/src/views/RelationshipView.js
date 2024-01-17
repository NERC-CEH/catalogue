import _ from 'underscore'
import template from '../templates/Relationship'
import ObjectInputView from './ObjectInputView'

export default ObjectInputView.extend({

    optionTemplate: _.template('<option value="<%= value %>"><%= label %></option>'),

    initialize (options) {
        this.template = template
        this.options = options.options
        ObjectInputView.prototype.initialize.call(this, options)
    },

    render () {
        ObjectInputView.prototype.render.apply(this)
        this.options.forEach(option => {
            return this.$('datalist').append(this.optionTemplate(option))
        })
        return this
    }
})
