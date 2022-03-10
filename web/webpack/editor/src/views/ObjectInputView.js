import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import validationTemplate from '../templates/Validation.tpl'
import template from '../templates/MapReprojection.tpl'

export default Backbone.View.extend({

  events: {
    change: 'modify'
  },

  initialize (options) {
    if (typeof this.template === 'undefined') {
      this.template = _.template(template)
    }
    this.data = options
    this.listenTo(this.model, 'remove', function () { return (this.remove)() })
    this.listenTo(this.model, 'change', function (model) {
      const $validation = this.$('>.validation')
      if (model.isValid()) {
        return $validation.hide()
      } else {
        $validation.show()
        const $validationList = $('div.warnings', $validation)
        $validationList.html('')
        return _.each(model.validationError, error => $validationList.append($(`<p>${error.message}</p>`)))
      }
    })
    return this.render()
  },

  render () {
    this.$el.html(this.template({ data: _.extend({}, this.data, this.model.attributes) }))
    this.$el.append(validationTemplate())
    return this
  },

  modify (event) {
    const $target = $(event.target)
    const name = $target.data('name')
    const value = $target.val()

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
      return element.append(newView.el)
    }.bind(this)

    const resetView = () => {
      (element.empty)()
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
