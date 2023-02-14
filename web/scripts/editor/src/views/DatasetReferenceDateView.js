import ObjectInputView from './ObjectInputView'
import template from '../templates/DatasetReferenceDate.tpl'
import _ from 'underscore'
import 'air-datepicker'
import 'air-datepicker/dist/js/i18n/datepicker.en.js'
import 'air-datepicker/dist/css/datepicker.min.css'
import Moment from 'moment'
export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)

    const that = this
    $(document).ready(function () {
      that.$('#input-creationDate').datepicker({
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        position: 'top left',
        onSelect: function (formattedDate, date, inst) {
          that.model.set('creationDate', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-creationDate').value = that.model.set('creationDate', Moment(date).format('YYYY-MM-DD'))
        }
      })

      that.$('#input-publicationDate').datepicker(
        {
          language: 'en',
          dateFormat: 'yyyy-mm-dd',
          position: 'top left',
          onSelect: function (formattedDate, date, inst) {
            that.model.set('publicationDate', Moment(date).format('YYYY-MM-DD'))
            that.$('#input-publicationDate').value = that.model.set('publicationDate', Moment(date).format('YYYY-MM-DD'))
          }
        })

      that.$('#input-unavailableDate').datepicker({
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        position: 'top left',
        onSelect: function (formattedDate, date, inst) {
          that.model.set('unavailableDate', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-unavailableDate').value = that.model.set('unavailableDate', Moment(date).format('YYYY-MM-DD'))
        }
      })

      that.$('#input-releasedDate').datepicker({
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        position: 'top left',
        onSelect: function (formattedDate, date, inst) {
          that.model.set('releasedDate', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-releasedDate').value = that.model.set('releasedDate', Moment(date).format('YYYY-MM-DD'))
        }
      })

      that.$('#input-supersededDate').datepicker({
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        position: 'top left',
        onSelect: function (formattedDate, date, inst) {
          that.model.set('supersededDate', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-supersededDate').value = this.model.set('supersededDate', Moment(date).format('YYYY-MM-DD'))
        }
      })
    })
    return this
  }
})
