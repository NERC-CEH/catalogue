import ObjectInputView from './ObjectInputView'
import template from '../templates/DatasetReferenceDate.tpl'
import _ from "underscore/underscore-node";

export default ObjectInputView.extend({

    render () {
      this.template = _.template(template)
      ObjectInputView.prototype.render.apply(this)
      this.$('input').datepicker({ dateFormat: 'yy-mm-dd' })
      return this
    }
})
