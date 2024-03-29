import _ from 'underscore'
import $ from 'jquery'
import SingleView from '../SingleView'
import parentTemplate from '../templates/Parent'
import childTemplate from '../templates/MultiString'
import template from '../templates/ChildLarge'

export default SingleView.extend({

  events: {
    change: 'modify',
    'click .remove': 'removeChild',
    'click .add': 'addChild'
  },

  initialize (options) {
    if (typeof this.template === 'undefined') {
      this.template = template
    }
    if (typeof this.childTemplate === 'undefined') {
      this.childTemplate = childTemplate
    }
    if (typeof this.parentTemplate === 'undefined') {
      this.parentTemplate = parentTemplate
    }
    SingleView.prototype.initialize.call(this, options)
    this.array = this.model.has(this.data.modelAttribute) ? _.clone(this.model.get(this.data.modelAttribute)) : []
    this.render()
  },

  renderParent () {
    this.$el.html(this.parentTemplate({ data: this.data }))
  },

  render () {
    this.renderParent()
    _.each(this.array, (string, index) => {
      return this.$('.existing').append(this.childTemplate({
        data: _.extend({}, this.data, {
          index,
          value: string
        })
      }))
    })
    return this
  },

  modify (event) {
    const $target = $(event.target)
    const index = $target.data('index')
    const value = $target.val()
    this.array.splice(index, 1, value)
    this.updateModel()
  },

  removeChild (event) {
    event.preventDefault()
    const $target = $(event.currentTarget)
    const index = $target.data('index')
    this.array.splice(index, 1)
    this.$(`#input${this.data.modelAttribute}${index}`).remove()
    this.updateModel()
  },

  addChild (event) {
    event.preventDefault()
    this.array.push('')
    const index = this.array.length - 1
    this.$('.existing').append(this.childTemplate({
      data: _.extend({}, this.data,
        { index })
    }))
    this.$(`#input${this.data.modelAttribute}${index} input`).focus()
    this.updateModel()
  },

  updateModel () {
    this.model.set(this.data.modelAttribute, _.clone(this.array))
  }
})
