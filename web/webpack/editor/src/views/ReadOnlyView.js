import _ from 'underscore'
import { SingleView } from '../index'
import template from '../templates/ReadOnly.tpl'
import $ from 'jquery'

export default SingleView.extend({

  events: {
    change: 'modify'
  },

  initialize (options) {
    this.template = _.template(template)
    SingleView.prototype.initialize.call(this, options);
    (this.render)()
    return this.listenTo(this.model, `change:${this.data.modelAttribute}`, this.render)
  },

  render () {
    SingleView.prototype.render.apply(this)
    return this.$('.dataentry').append(this.template({ data: _.extend({}, this.data, { value: this.model.get(this.data.modelAttribute) }) }))
  },

  modify (event) {
    const $target = $(event.target)
    const name = $target.data('name')
    const value = $target.val()

    if (!value) {
      return this.model.unset(name)
    } else {
      return this.model.set(name, value)
    }
  }
})
