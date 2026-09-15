import ObjectInputView from './ObjectInputView'
import template from '../templates/SampleStorageLocation'

export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)

    this.$('#input-archive').val(this.model.get('archive'))
    this.$('#input-locale').val(this.model.get('locale'))
    this.$('#input-shelf').val(this.model.get('shelf'))
  }
})
