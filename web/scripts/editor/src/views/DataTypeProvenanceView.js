import ObjectInputView from './ObjectInputView'
import template from '../templates/DataTypeProvenance.tpl'
import ParentStringView from './ParentStringView'
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

      that.$('#input-modificationDate').datepicker({
        language: 'en',
        dateFormat: 'yyyy-mm-dd',
        position: 'top left',
        onSelect: function (formattedDate, date, inst) {
          that.model.set('modificationDate', Moment(date).format('YYYY-MM-DD'))
          that.$('#input-modificationDate').value = that.model.set('creationDate', Moment(date).format('YYYY-MM-DD'))
        }
      })

      new ParentStringView({
        el: that.$('#provenanceContributors'),
        model: that.model,
        modelAttribute: 'contributors',
        label: 'Contributors'
      })
    })
    return this
  }
})
