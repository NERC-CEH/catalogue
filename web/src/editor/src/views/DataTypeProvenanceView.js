/* eslint no-new: "off" */
import ObjectInputView from './ObjectInputView'
import template from '../templates/DataTypeProvenance'
import ParentStringView from './ParentStringView'
import { formatDateForInput } from '../utils'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)

    const creationDate = formatDateForInput(this.model.get('creationDate'))
    const modificationDate = formatDateForInput(this.model.get('modificationDate'))
    this.$('#input-creationDate').val(creationDate)
    this.$('#input-modificationDate').val(modificationDate)

    this.$('#input-creationDate').on('input', (event) => {
      this.model.set('creationDate', event.target.value)
    })

    this.$('#input-modificationDate').on('input', (event) => {
      this.model.set('modificationDate', event.value)
    })

    new ParentStringView({
      el: this.$('#provenanceContributors'),
      model: this.model,
      modelAttribute: 'contributors',
      label: 'Contributors'
    })
  }
})
