import ObjectInputView from './ObjectInputView'
import template from '../templates/TemporalExtent.tpl'
import _ from 'underscore'
import 'air-datepicker'
import 'air-datepicker/dist/js/i18n/datepicker.en.js'
import 'air-datepicker/dist/css/datepicker.min.css'
import Moment from 'moment'

// difference must be in parent view vs single view
export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    const that = this

    let beginStartDate
    let endStartDate
    if (that.model.get('begin')) {
      beginStartDate = new Date(that.model.get('begin'))
    } else {
      beginStartDate = new Date()
    }

    if (that.model.get('end')) {
      endStartDate = new Date(that.model.get('end'))
    } else {
      endStartDate = beginStartDate
    }

    that.$('.input-begin').datepicker({
      language: 'en',
      dateFormat: 'yyyy-mm-dd',
      startDate: beginStartDate,
      position: 'top left',
      onSelect: function (formattedDate, date) {
        that.model.set('begin', Moment(date).format('YYYY-MM-DD'))
        that.$('#input-begin').value = that.model.set('begin', Moment(date).format('YYYY-MM-DD'))
      }
    })

    that.$('.input-end').datepicker({
      language: 'en',
      minDate: beginStartDate,
      dateFormat: 'yyyy-mm-dd',
      startDate: endStartDate,
      position: 'top left',
      onSelect: function (formattedDate, date) {
        that.model.set('end', Moment(date).format('YYYY-MM-DD'))
        that.$('#input-end').value = that.model.set('end', Moment(date).format('YYYY-MM-DD'))
      }
    })
    return this
  }
})
