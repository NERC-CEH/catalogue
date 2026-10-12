import _ from 'underscore'
import Backbone from 'backbone'
import childTemplate from '../templates/ChildLarge'

export default Backbone.View.extend({

  className: 'row',

  events: {
    'click button.remove': 'delete',
    'click button.showhide': 'showHide'
  },

  initialize (options) {
    this.childTemplate = childTemplate
    this.data = options
    this.listenTo(this.model, 'remove', function () { this.remove() })
    this.listenTo(this.model, 'showhide', function () { this.showHide() })
    // Templates build element ids from the index, so it has to be unique on the whole page: a
    // position is only unique within one collection, and goes stale when rows are removed or moved
    this.index = this.model.cid
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
  },

  showHide () {
    this.$('.extended').toggleClass('d-none')
    this.$('.showhide span').toggleClass('fa-chevron-down')
    this.$('.showhide span').toggleClass('fa-chevron-up')
  }
})
