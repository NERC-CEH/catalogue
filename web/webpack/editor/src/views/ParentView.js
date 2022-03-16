import _ from 'underscore'
import { Positionable } from '../collections'
import SingleView from '../SingleView'
import ChildView from './ChildView'
import template from '../templates/Parent.tpl'

export default SingleView.extend({

  events: {
    'click button.add': 'add'
  },

  initialize (options) {
    if (typeof this.template === 'undefined') {
      this.template = _.template(template)
    }
    SingleView.prototype.initialize.call(this, options)
    this.collection = new Positionable([], { model: this.data.ModelType })

    this.listenTo(this.collection, 'add', this.addOne)
    this.listenTo(this.collection, 'reset', this.addAll)
    this.listenTo(this.collection, 'add remove change position', this.updateModel)
    this.listenTo(this.model, 'sync', this.updateCollection)

    this.render()
    this.$attach = this.$('.existing')
    this.collection.reset(this.getModelData())

    if (this.data.multiline) {
      this.$el.addClass('multiline')
    }

    if (!(this.data.disabled === 'disabled')) {
      return this.$attach.sortable({
        start: (event, ui) => {
          this._oldPosition = ui.item.index()
        },
        update: (event, ui) => {
          this.collection.position(this._oldPosition, ui.item.index())
        }
      })
    }
  },

  render () {
    this.$el.html(this.template({ data: this.data }))
    return this
  },

  addOne (model) {
    const view = new ChildView(_.extend({}, this.data,
      { model })
    )
    return this.$attach.append(view.el)
  },

  addAll () {
    this.$attach.html('')
    return this.collection.each(this.addOne, this)
  },

  add () {
    return this.collection.add(new this.data.ModelType())
  },

  getModelData () {
    let model = this.model.attributes
    const path = this.data.modelAttribute.split('.')
    while (path.length >= 2) {
      model = model[path.shift()] || {}
    }

    return model[path[0]] || []
  },

  updateModel () {
    const path = this.data.modelAttribute.split('.')
    let data = this.collection.toJSON()

    while (path.length > 0) {
      const oldData = data
      data = {}
      data[path.pop()] = oldData
    }
    return this.model.set(data)
  },

  updateCollection (model) {
    if (model.hasChanged(this.data.modelAttribute)) {
      const updated = model.get(this.data.modelAttribute)

      const collectionLength = this.collection.length
      // Update existing models
      _.chain(updated)
        .first(collectionLength)
        .each((update, index) => {
          return this.collection
            .at(index)
            .set(update)
        })
        // Add new models
      _.chain(updated)
        .rest(collectionLength)
        .each(update => {
          return this.collection.add(update)
        }
        )
        // Remove models not in updated
      return this.collection.remove(this.collection.rest(updated.length))
    }
  },

  show () {
    SingleView.prototype.show.apply(this)
    return this.collection.trigger('visible')
  },

  hide () {
    SingleView.prototype.hide.apply(this)
    return this.collection.trigger('hidden')
  }
})
