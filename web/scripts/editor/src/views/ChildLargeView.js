import _ from 'underscore'
import Backbone from 'backbone'
import childTemplate from '../templates/ChildLarge.tpl'
import $ from 'jquery'

export default Backbone.View.extend({

  className: 'row',

  events: {
    'click button.remove': 'delete',
    'click button.showhide': 'showHide'
  },

  initialize (options) {
    this.childTemplate = _.template(childTemplate)
    this.data = options
    this.listenTo(this.model, 'remove', function () { this.remove() })
    this.listenTo(this.model, 'showhide', function () { this.showHide() })
    this.index = this.model.collection.indexOf(this.model)
    this.render()
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
    this.$('.extended').toggleClass('hidden')
    this.$('.showhide span').toggleClass('fa-chevron-down')
    this.$('.showhide span').toggleClass('fa-chevron-up')
  }
})
