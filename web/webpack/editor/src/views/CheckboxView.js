import { InputView } from '../index'
import template from '../templates/Checkbox.tpl'
import _ from 'underscore'

export default InputView.extend({

  render () {
    this.template = _.template(template)
    InputView.prototype.render.apply(this)
    this.$('[type="checkbox"]').prop('checked', this.model.get(this.data.modelAttribute))
    return this
  },

  modify (event) {
    const $target = $(event.target)
    const name = $target.data('name')
    const value = $target.prop('checked')
    return this.model.set(name, value)
  }
})
