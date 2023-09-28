import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/DatasetReferenceDate'
import 'air-datepicker'
import Moment from 'moment'
export default ObjectInputView.extend({

  initialize () {
    this.template = template
  },

  render () {
    ObjectInputView.prototype.render.apply(this)

    const that = this
    $(document).ready(function () {
      that.$('#input-creationDate').datepicker({
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        position: 'top left',
        onSelect: function (formattedDate, date) {
          that.model.set('creationDate', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-creationDate').value = that.model.set('creationDate', Moment(date).format('YYYY-MM-DD'))
        }
      })

      that.$('#input-publicationDate').datepicker(
        {
          language: 'en',
          dateFormat: 'yyyy-mm-dd',
          position: 'top left',
          onSelect: function (formattedDate, date) {
            that.model.set('publicationDate', Moment(date).format('YYYY-MM-DD'))
            that.$('#input-publicationDate').value = that.model.set('publicationDate', Moment(date).format('YYYY-MM-DD'))
          }
        })

      that.$('#input-unavailableDate').datepicker({
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        position: 'top left',
        onSelect: function (formattedDate, date) {
          that.model.set('unavailableDate', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-unavailableDate').value = that.model.set('unavailableDate', Moment(date).format('YYYY-MM-DD'))
        }
      })

      that.$('#input-releasedDate').datepicker({
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        position: 'top left',
        onSelect: function (formattedDate, date) {
          that.model.set('releasedDate', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-releasedDate').value = that.model.set('releasedDate', Moment(date).format('YYYY-MM-DD'))
        }
      })

      that.$('#input-supersededDate').datepicker({
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        position: 'top left',
        onSelect: function (formattedDate, date) {
          that.model.set('supersededDate', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-supersededDate').value = this.model.set('supersededDate', Moment(date).format('YYYY-MM-DD'))
        }
      })
    })
    return this
  }
})
