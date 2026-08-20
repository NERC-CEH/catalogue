import _ from 'underscore'
import $ from 'jquery'
import SingleView from '../SingleView'
import parentTemplate from '../templates/Parent'
import childTemplate from '../templates/MultiString'
import template from '../templates/ChildLarge'
import { cleanText, hasContent } from '../utils'

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
    const value = cleanText($target.val())
    this.array.splice(index, 1, value)
    $target.val(value)
    this.updateModel()
  },

  removeChild (event) {
    event.preventDefault()
    const $target = $(event.currentTarget)
    const index = $target.data('index')
    this.array.splice(index, 1)
    // Rows are addressed by their data-index attribute, so removing just the deleted row's element
    // would leave every later row pointing at the wrong slot. Re-render instead: render() is the one
    // place indices are assigned, so the surviving rows come back renumbered from the array.
    this.render()
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
    // A row added but never typed into is an empty string. It stays in this.array so the form keeps
    // showing it and the DOM data-index attributes still line up, but it must not reach the record.
    this.model.set(this.data.modelAttribute, this.array.filter(entry => hasContent(entry)))
  }
})
