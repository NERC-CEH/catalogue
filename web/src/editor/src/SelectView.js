import _ from 'underscore'
import InputView from './InputView'

const template = _.template('<select data-name="<%= data.modelAttribute %>" class="editor-input" id="input-<%= data.modelAttribute %>"></select>')
export default InputView.extend({

  initialize (options) {
    this.optionTemplate = _.template('<option value="<%= value %>"><%= label %></option>')
    this.options = options.options
    this.template = template
    InputView.prototype.initialize.call(this, options)
  },

  render () {
    InputView.prototype.render.apply(this)
    this.options.forEach(option => {
      this.$('select').append(this.optionTemplate(option))
    })
    this.$('select').val(this.model.get(this.data.modelAttribute))
    return this
  }
})
