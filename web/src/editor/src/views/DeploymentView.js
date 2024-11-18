import ObjectInputView from './ObjectInputView'
import template from '../templates/deployment'
import AirDatepicker from 'air-datepicker'
import localeEn from 'air-datepicker/locale/en'

const datepickerOptions = {
  dateFormat: 'yyyy-MM-dd',
  locale: localeEn,
  isMobile: true,
  autoClose: true
}

export default ObjectInputView.extend({
  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
  },

  render () {
    ObjectInputView.prototype.render.call(this)
    new AirDatepicker(this.$('.deployment-start')[0], {
      ...datepickerOptions,
      onSelect: ({ formattedDate }) => this.model.set('start', formattedDate)
    })
    new AirDatepicker(this.$('.deployment-end')[0], {
      ...datepickerOptions,
      onSelect: ({ formattedDate }) => this.model.set('end', formattedDate)
    })
  }
})
