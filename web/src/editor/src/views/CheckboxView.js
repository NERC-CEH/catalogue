import _ from 'underscore'
import $ from 'jquery'
import InputView from '../InputView'
import template from '../templates/Checkbox.tpl'

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
    this.model.set(name, value)
  }
})
