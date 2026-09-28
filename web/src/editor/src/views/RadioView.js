import _ from 'underscore'
import $ from 'jquery'
import SingleView from '../SingleView'

const template = _.template(`
<div class="form-check form-check-inline">
  <input data-name="<%= data.modelAttribute %>" id="<%= data.modelAttribute %>-yes" class="form-check-input" type="radio" name="<%= data.modelAttribute %>" value="true">
  <label class="form-check-label" for="<%= data.modelAttribute %>-yes">Yes</label>
</div>
<div class="form-check form-check-inline">
  <input data-name="<%= data.modelAttribute %>" id="<%= data.modelAttribute %>-no" class="form-check-input" type="radio" name="<%= data.modelAttribute %>" value="false">
  <label class="form-check-label" for="<%= data.modelAttribute %>-no">No</label>
</div>
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
    this.$('.dataentry').append(template({ data: this.data }))
    if (value === true) {
      this.$(`#${this.data.modelAttribute}-yes`).prop('checked', true)
    } else if (value === false) {
      this.$(`#${this.data.modelAttribute}-no`).prop('checked', true)
    }
    if (this.data.readonly) {
      this.$(':input').prop('disabled', true)
    }
    return this
  },

  modify (event) {
    const $target = $(event.target)
    const name = $target.data('name')
    const value = $target.val() === 'true'
    this.model.set(name, value)
  }
})
