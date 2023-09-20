import _ from 'underscore'
import $ from 'jquery'
import SingleView from '../SingleView'
import template from '../templates/ReadOnly'

export default SingleView.extend({

  events: {
    change: 'modify'
  },

  initialize (options) {
    this.template = template
    SingleView.prototype.initialize.call(this, options)
    this.render()
    this.listenTo(this.model, `change:${this.data.modelAttribute}`, this.render)
  },

  render () {
    SingleView.prototype.render.apply(this)
    this.$('.dataentry').append(this.template({ data: _.extend({}, this.data, { value: this.model.get(this.data.modelAttribute) }) }))
    return this
  },

  modify (event) {
    const $target = $(event.target)
    const name = $target.data('name')
    const value = $target.val()

    if (!value) {
      this.model.unset(name)
    } else {
      this.model.set(name, value)
    }
  }
})
