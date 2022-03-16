import _ from 'underscore'
import Backbone from 'backbone'
import template from '../templates/ChildLarge.tpl'

export default Backbone.View.extend({

  className: 'row',

  events: {
    'click button.remove': 'delete',
    'click button.showhide': 'showhide'
  },

  initialize (options) {
    this.template = _.template(template)
    this.data = options
    this.listenTo(this.model, 'remove', function () { return (this.remove)() })
    this.listenTo(this.model, 'showhide', function () { return (this.showhide)() })
    this.index = this.model.collection.indexOf(this.model);
    (this.render)()
    return new this.data.ObjectInputView(_.extend({}, this.data, {
      el: this.$('.dataentry'),
      model: this.model,
      index: this.index
    }
    )
    )
  },

  render () {
    this.$el.html(this.template({ index: this.index, data: this.data }))
    return this
  },

  delete () {
    return this.model.collection.remove(this.model)
  },

  showhide () {
    if (this.$('.extended').hasClass('hidden')) {
      this.$('.extended').removeClass('hidden')
      this.$('.showhide span').removeClass('fa-chevron-down')
      return this.$('.showhide span').addClass('fa-chevron-up')
    } else {
      this.$('.extended').addClass('hidden')
      this.$('.showhide span').removeClass('fa-chevron-up')
      return this.$('.showhide span').addClass('fa-chevron-down')
    }
  }
})
