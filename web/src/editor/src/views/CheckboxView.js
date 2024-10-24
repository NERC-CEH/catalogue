import _ from 'underscore'
import $ from 'jquery'
import SingleView from '../SingleView'

const template = _.template(`
<input data-name="<%= data.modelAttribute %>" id="input-<%= data.modelAttribute %>" value="<%= data.value %>" type="checkbox" >
`)

export default SingleView.extend({

  events: {
    change: 'modify'
  },

  initialize (options) {
    SingleView.prototype.initialize.call(this, options)
    this.listenTo(this.model, `change:${this.data.modelAttribute}`, this.render)
    this.render()
  },

  render () {
    SingleView.prototype.render.apply(this)
    const value = this.model.get(this.data.modelAttribute)
    this.$('.dataentry').append(template({ data: { ...this.data, value } }))
    this.$('[type="checkbox"]').prop('checked', value)
    if (this.data.readonly) {
      this.$(':input').prop('readonly', true)
    }
    return this
  },

  modify (event) {
    const $target = $(event.target)
    const name = $target.data('name')
    const value = $target.prop('checked')
    this.model.set(name, value)
  }
})
