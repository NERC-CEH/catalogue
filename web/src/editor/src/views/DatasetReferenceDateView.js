/* eslint no-new: "off" */
import _ from 'underscore'
import AirDatepicker from 'air-datepicker'
import localeEn from 'air-datepicker/locale/en'
import ObjectInputView from './ObjectInputView'
import template from '../templates/DatasetReferenceDate'

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
        new AirDatepicker('#input-publicationDate', _.extend({}, datepickerOptions, {
            onSelect ({ formattedDate }) {
                that.model.set('publicationDate', formattedDate)
            }
        }))
        new AirDatepicker('#input-unavailableDate', _.extend({}, datepickerOptions, {
            onSelect ({ formattedDate }) {
                that.model.set('unavailableDate', formattedDate)
            }
        }))
        new AirDatepicker('#input-releasedDate', _.extend({}, datepickerOptions, {
            onSelect ({ formattedDate }) {
                that.model.set('releasedDate', formattedDate)
            }
        }))
        new AirDatepicker('#input-supersededDate', _.extend({}, datepickerOptions, {
            onSelect ({ formattedDate }) {
                that.model.set('supersededDate', formattedDate)
            }
        }))
    }
})
