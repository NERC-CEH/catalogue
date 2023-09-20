import _ from 'underscore'
import $ from 'jquery'
import SingleView from './SingleView'
import template from './inputTemplate'

export default SingleView.extend({

  events: {
    change: 'modify'
  },

  initialize (options) {
    SingleView.prototype.initialize.call(this, options)
    this.template = template
    this.render()
    this.listenTo(this.model, `change:${this.data.modelAttribute}`, this.render)
  },

  render () {
    SingleView.prototype.render.apply(this)
    this.$('.dataentry').append(this.template({ data: _.extend({}, this.data, { value: this.model.get(this.data.modelAttribute) }) }))
    if (this.data.readonly) {
      this.$(':input').prop('readonly', true)
    }
    return this
  },

  modify (event) {
    const name = $(event.target).data('name')
    const value = $(event.target).val()

    if (!value) {
      this.model.unset(name)
    } else {
      this.model.set(name, value)
    }
  }
})
