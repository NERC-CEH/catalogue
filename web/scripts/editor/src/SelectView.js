import _ from 'underscore'
import InputView from './InputView'
import template from './Select.tpl'

export default InputView.extend({

  initialize (options) {
    this.optionTemplate = _.template('<option value="<%= value %>"><%= label %></option>')
    this.options = options.options
    this.template = _.template(template)
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
