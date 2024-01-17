/* eslint no-new: "off" */
import ObjectInputView from './ObjectInputView'
import template from '../templates/temporalExtent'
import AirDatepicker from 'air-datepicker'
import localeEn from 'air-datepicker/locale/en'

const datepickerOptions = {
    dateFormat: 'yyyy-MM-dd',
    locale: localeEn,
    position: 'top left'
}

export default ObjectInputView.extend({
    initialize (options) {
        this.template = template
        ObjectInputView.prototype.initialize.call(this, options)
    },

    render () {
        ObjectInputView.prototype.render.call(this)
        new AirDatepicker(this.$('#input-begin')[0], {
            ...datepickerOptions,
            onSelect: ({ formattedDate }) => this.model.set('begin', formattedDate)
        })
        new AirDatepicker(this.$('#input-end')[0], {
            ...datepickerOptions,
            onSelect: ({ formattedDate }) => this.model.set('end', formattedDate)
        })
    }
})
