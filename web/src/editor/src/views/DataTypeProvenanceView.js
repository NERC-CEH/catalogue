import _ from 'underscore'
import $ from 'jquery'
import ObjectInputView from './ObjectInputView'
import template from '../templates/DataTypeProvenance.tpl'
import ParentStringView from './ParentStringView'
import 'air-datepicker'
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

      /* eslint no-new: "off" */
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
