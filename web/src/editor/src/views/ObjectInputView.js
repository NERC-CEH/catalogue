import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import validationTemplate from '../templates/validation'

export default Backbone.View.extend({

    events: {
        change: 'modify'
    },

    initialize (options) {
        this.data = options
        this.listenTo(this.model, 'remove', function () { this.remove() })
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
        this.$el.append(validationTemplate())
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
        return false // disable bubbling
    },

    /*
    Defines a sortable list view which is bound to a positionable collection.
    The supplied `view` callback function is required to generate a constructed
    child view element which will be rendered on to the list
    */
    createList (collection, selector, view) {
        const element = this.$(selector)
        const that = this
        const addView = function () {
            const newView = view.apply(that, arguments)
            element.append(newView.el)
        }

        const resetView = () => {
            element.empty()
            collection.each(addView, this)
        }

        this.listenTo(collection, 'add', addView)
        this.listenTo(collection, 'reset', resetView)

        let pos = null
        if (this.data.disabled !== 'disabled') {
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
