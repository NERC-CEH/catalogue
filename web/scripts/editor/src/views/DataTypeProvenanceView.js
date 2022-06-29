import ObjectInputView from './ObjectInputView'
import template from '../templates/DatasetReferenceDate.tpl'
import ParentStringView from './ParentStringView'
import _ from 'underscore'

export default ObjectInputView.extend({

  render () {
    this.template = _.template(template)
    ObjectInputView.prototype.render.apply(this)
    this.$('input').datepicker({ dateFormat: 'yy-mm-dd' })

    // eslint-disable-next-line no-unused-vars
    const parent = new ParentStringView({
      el: this.$('#provenanceContributors'),
      model: this.model,
      modelAttribute: 'contributors',
      label: 'Contributors'
    })
    return this
  }
})