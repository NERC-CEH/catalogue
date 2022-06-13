import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import validationTemplate from '../templates/Validation.tpl'

export default Backbone.View.extend({

  events: {
    change: 'modify'
  },

  initialize (options) {
    this.data = options
    this.listenTo(this.model, 'remove', function () { return this.remove() })
    this.listenTo(this.model, 'change', function (model) {
      if (model.isValid()) {
        this.$('>.validation').hide()
      } else {
        this.$('>.validation').show()
        $('div.warnings', this.$('>.validation')).html('')
        _.each(model.validationError, error => $('div.warnings', this.$('>.validation')).append($(`<p>${error.message}</p>`)))
      }
    })
    this.render()
  },

  render () {
    this.$el.html(this.template({ data: _.extend({}, this.data, this.model.attributes) }))
    this.$el.append(_.template(validationTemplate))
    return this
  },

  modify (event) {
    const name = $(event.target).data('name')
    const value = $(event.target).val()

    if (!value) {
      this.model.unset(name)
    } else {
      this.model.set(name, value)
    }

    return false
  }, // disable bubbling

  /*
  Defines a sortable list view which is bound to a positionable collection.
  The supplied `view` callback function is required to generate a constructed
  child view element which will be renederd on to the list
  */
  createList (collection, selector, view) {
    const element = this.$(selector)
    const addView = function () {
      const newView = view.apply(this, arguments)
      return element.append(newView.el) // no element here for mapdata source iew
    }.bind(this)

    const resetView = () => {
      element.empty()
      return collection.each(addView, this)
    }

    this.listenTo(collection, 'add', addView)
    this.listenTo(collection, 'reset', resetView)

    let pos = null
    if (!(this.data.disabled === 'disabled')) {
      element.sortable({
        start: (event, ui) => {
          pos = ui.item.index()
        },
        update: (event, ui) => {
          collection.position(pos, ui.item.index())
        }
      })
    }

    resetView()
    return collection
  }
})
