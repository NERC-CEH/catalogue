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

    this.$('#input-creationDate').datepicker({
      language: 'en',
      dateFormat: 'yyyy-mm-dd',
      position: 'top left',
      onSelect: function (formattedDate, date, inst) {
        this.model.set('creationDate', Moment(date).format('YYYY-MM-DD'))
        this.$('#input-creationDate').value = this.model.set('creationDate', Moment(date).format('YYYY-MM-DD'))
      }
    })

    this.$('#input-modificationDate').datepicker({
      language: 'en',
      dateFormat: 'yyyy-mm-dd',
      position: 'top left',
      onSelect: function (formattedDate, date, inst) {
        this.model.set('modificationDate', Moment(date).format('YYYY-MM-DD'))
        this.$('#input-modificationDate').value = that.model.set('creationDate', Moment(date).format('YYYY-MM-DD'))
      }
    })

    new ParentStringView({
      el: this.$('#provenanceContributors'),
      model: this.model,
      modelAttribute: 'contributors',
      label: 'Contributors'
    })
    return this
  }
})
