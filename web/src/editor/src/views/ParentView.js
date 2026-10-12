import _ from 'underscore'
import 'jquery-ui/ui/widgets/sortable'
import SingleView from '../SingleView'
import ChildView from './ChildView'
import template from '../templates/Parent'
import { Positionable } from '../collections'
import { hasContent } from '../utils'
import { fetchKeywordsFromLegilo } from './LegiloFetcher'
import keywordsTemplate from '../templates/LegiloKeywords'
import LegiloView from './LegiloView'
import { keywordOnSelect } from './LegiloEventHandler'
import { LegiloKeyword } from '../models'

export default SingleView.extend({

  events: {
    'click button.add': 'add'
  },

  initialize (options) {
    if (typeof this.template === 'undefined') {
      this.template = template
    }
    SingleView.prototype.initialize.call(this, options)
    this.collection = new Positionable([], { model: this.data.ModelType })

    this.listenTo(this.collection, 'add', this.addOne)
    this.listenTo(this.collection, 'reset', this.addAll)
    this.listenTo(this.collection, 'add remove change position', this.updateModel)
    this.listenTo(this.model, 'sync', this.updateCollection)

    this.render()
    this.makeSortable()
    this.collection.reset(this.getModelData())

    if (this.data.multiline) {
      this.$el.addClass('multiline')
    }

    if ((this.data.fetchKeywordsButton || this.data.renderLegiloKeywords) && this.model.get('id')) {
      this.legiloKeywords = new LegiloView({
        collection: this.collection,
        model: this.model,
        modelType: LegiloKeyword,
        template: keywordsTemplate,
        fetcher: fetchKeywordsFromLegilo,
        fetchButton: this.$('.legilo-keywords-btn'),
        result: this.$('.legilo-keywords-view'),
        locationSelectEl: this.$('#location-select'),
        onSelect: keywordOnSelect
      })
      if (this.data.renderLegiloKeywords) {
        this.listenTo(this.model, 'change:' + this.legiloKeywords.fetcher.name, this.legiloKeywords.render.bind(this.legiloKeywords))
      }
    }
  },

  render () {
    this.$el.html(this.template({ data: this.data }))
    return this
  },

  makeSortable () {
    if (this.data.disabled !== 'disabled' && this.data.modelAttribute !== 'boundingBoxes') {
      this.$('.existing').sortable({
        start: (event, ui) => {
          this._oldPosition = ui.item.index()
        },
        update: (event, ui) => {
          this.collection.position(this._oldPosition, ui.item.index())
        }
      })
    }
  },

  addOne (model) {
    const view = new ChildView(_.extend({}, this.data, { model, parentModel: this.model }))
    this.$('.existing').append(view.el)
  },

  addAll () {
    this.$('.existing').html('')
    this.collection.each(this.addOne, this)
  },

  add () {
    this.collection.add(new this.data.ModelType())
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
    // A row that was added but never filled in is an attribute-less model. It belongs in the
    // collection, so the form keeps showing it, but not in the record.
    let data = this.collection.toJSON().filter(entry => hasContent(entry))

    while (path.length > 0) {
      const oldData = data
      data = {}
      data[path.pop()] = oldData
    }
    this.model.set(data)
  },

  updateCollection (model) {
    if (model.hasChanged(this.data.modelAttribute)) {
      const updated = model.get(this.data.modelAttribute)

      const collectionLength = this.collection.length
      // Update existing models
      _.chain(updated)
        .first(collectionLength)
        .each((update, index) => {
          this.collection
            .at(index)
            .set(update)
        })
      // Add new models
      _.chain(updated)
        .rest(collectionLength)
        .each(update => {
          this.collection.add(update)
        })
      // Remove models not in updated
      this.collection.remove(this.collection.rest(updated.length))
    }
  },

  show () {
    SingleView.prototype.show.apply(this)
    this.collection.trigger('visible')
  },

  hide () {
    SingleView.prototype.hide.apply(this)
    this.collection.trigger('hidden')
  }
})
