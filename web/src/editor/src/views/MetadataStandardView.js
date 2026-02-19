import ObjectInputView from './ObjectInputView'
import template from '../templates/MetadataStandard'
import { formatDateForInput } from '../utils'

export default ObjectInputView.extend({
  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)

    const date = formatDateForInput(this.model.get('date'))
    this.$(`#metadataStandard${this.data.index}Date`).val(date)
    this.$(`#metadataStandard${this.data.index}Date`).on('input', (event) => {this.model.set('date', this.value)})
  },
  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('conformity'))
    return this
  }
})
