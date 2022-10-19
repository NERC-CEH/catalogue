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
    // eslint-disable-next-line no-unused-vars

    this.$('#input-creationDate').datepicker({
      language: 'en',
      dateFormat: 'yyyy-mm-dd',
      position: 'top left',
      onSelect: function (formattedDate, date, inst) {
        this.model.set('creationDate', Moment(date).format('YYYY-MM-DD'))
        this.$('#input-creationDate').value = this.model.set('creationDate', Moment(date).format('YYYY-MM-DD'))
      }
    })

    this.$('#input-publicationDate').datepicker(
      {
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        position: 'top left',
        onSelect: function (formattedDate, date, inst) {
          this.model.set('publicationDate', Moment(date).format('YYYY-MM-DD'))
          this.$('#input-publicationDate').value = this.model.set('publicationDate', Moment(date).format('YYYY-MM-DD'))
        }
      })

    this.$('#input-releasedDate').datepicker({
      language: 'en',
      dateFormat: 'yyyy-mm-dd',
      position: 'top left',
      onSelect: function (formattedDate, date, inst) {
        this.model.set('releasedDate', Moment(date).format('YYYY-MM-DD'))
        this.$('#input-releasedDate').value = this.model.set('releasedDate', Moment(date).format('YYYY-MM-DD'))
      }
    })

    this.$('#input-supersededDate').datepicker({
      language: 'en',
      dateFormat: 'yyyy-mm-dd',
      position: 'top left',
      onSelect: function (formattedDate, date, inst) {
        this.model.set('supersededDate', Moment(date).format('YYYY-MM-DD'))
        this.$('#input-supersededDate').value = this.model.set('supersededDate', Moment(date).format('YYYY-MM-DD'))
      }
    })
    return this
  }
})
