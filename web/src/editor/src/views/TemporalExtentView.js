/* eslint no-new: "off" */
import _ from 'underscore'
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
    // TODO: work out why these date pickers don't appear
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
    const that = this
    new AirDatepicker('.input-begin', _.extend({}, datepickerOptions, {
      onSelect ({ formattedDate }) {
        that.model.set('begin', formattedDate)
      }
    }))
    new AirDatepicker('.input-end', _.extend({}, datepickerOptions, {
      onSelect ({ formattedDate }) {
        that.model.set('end', formattedDate)
      }
    }))
  }
})
