/* eslint no-new: "off" */
import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import template from '../templates/DataTypeProvenance'
import ParentStringView from './ParentStringView'
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
    const that = this
    new AirDatepicker('#input-creationDate', _.extend({}, datepickerOptions, {
      onSelect ({ formattedDate }) {
        that.model.set('creationDate', formattedDate)
      }
    }))
    new AirDatepicker('#input-modificationDate', _.extend({}, datepickerOptions, {
      onSelect ({ formattedDate }) {
        that.model.set('modificationDate', formattedDate)
      }
    }))
    new ParentStringView({
      el: this.$('#provenanceContributors'),
      model: that.model,
      modelAttribute: 'contributors',
      label: 'Contributors'
    })
  }
})
