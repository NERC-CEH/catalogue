import _ from 'underscore'
import Backbone from 'backbone'
import template from '../templates/Child.tpl'

export default Backbone.View.extend({

    className: 'row',

    events: {
      'click button.remove': 'delete'
    },

    initialize (options) {
      this.template = _.template(template)
      this.data = options
      this.listenTo(this.model, 'remove', function () { return (this.remove)() })
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
      this.$el.html(template({ index: this.index, data: this.data }))
      return this
    },

    delete () {
      return this.model.collection.remove(this.model)
    }
})
