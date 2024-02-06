import _ from 'underscore'
import Backbone from 'backbone'
import childTemplate from '../templates/Child'

export default Backbone.View.extend({

  className: 'row',

  events: {
    'click button.editor-button-remove': 'delete'
  },

  initialize (options) {
    this.childTemplate = childTemplate
    this.data = options
    this.listenTo(this.model, 'remove', function () { this.remove() })
    this.index = this.model.collection.indexOf(this.model)
    this.render()
    // eslint-disable-next-line no-unused-vars
    const view = new this.data.ObjectInputView(_.extend({}, this.data, {
      el: this.$('.dataentry'),
      model: this.model,
      index: this.index
    }))
  },

  render () {
    this.$el.html(this.childTemplate({ index: this.index, data: this.data }))
    return this
  },

  delete () {
    this.model.collection.remove(this.model)
  }
})
