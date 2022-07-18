import ObjectInputView from './ObjectInputView'
import template from '../templates/TemporalExtent.tpl'
import _ from 'underscore'
import $ from 'jquery'
import 'air-datepicker'
import 'air-datepicker/dist/js/i18n/datepicker.en.js'
import 'air-datepicker/dist/css/datepicker.min.css'
import Moment from 'moment'

// difference must be in parent view vs single view
export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    // eslint-disable-next-line no-unused-vars

    const that = this
    $(document).ready(function () {
      that.$('#input-begin').datepicker({
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        startDate: new Date(that.model.get('begin')),
        position: 'top left',
        onSelect: function (formattedDate, date, inst) {
          that.model.set('begin', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-begin').value = that.model.set('begin', Moment(date).format('YYYY-MM-DD'))
        }
      })

      that.$('#input-end').datepicker({
        language: 'en',
        minDate: new Date(that.model.get('end')),
        dateFormat: 'yyyy-mm-dd',
        startDate: new Date(that.model.get('begin')),
        position: 'top left',
        onSelect: function (formattedDate, date, inst) {
          that.model.set('end', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-end').value = that.model.set('end', Moment(date).format('YYYY-MM-DD'))
        }
      })
      return this
    })
  }
})
