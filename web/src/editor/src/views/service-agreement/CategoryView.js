import { ObjectInputView } from '../index'
import _ from 'underscore'

const template = _.template(`
<select data-name="value" class="editor-input">
        <option value="Environmental data">Environmental Data</option>
        <option value="Information product">Information Product</option>
</select>
`)
export default ObjectInputView.extend({

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.$('select').val(this.model.get('value'))
    return this
  }
})
