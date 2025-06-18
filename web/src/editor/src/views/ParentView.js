import _ from 'underscore'
import $ from 'jquery'
import SingleView from '../SingleView'
import ChildView from './ChildView'
import template from '../templates/Parent'
import { Positionable } from '../collections'
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
    this.collection.reset(this.getModelData())

    if (this.data.multiline) {
      this.$el.addClass('multiline')
    }

    const that = this
    $(document).ready(function () {
      if ((that.data.fetchKeywordsButton || that.data.renderLegiloKeywords) && that.model.get('id')) {
        const locationSelect = that.$('#location-select')

        that.legiloKeywords = new LegiloView({
          collection: that.collection,
          model: that.model,
          modelType: LegiloKeyword,
          template: keywordsTemplate,
          fetcher: fetchKeywordsFromLegilo,
          fetchButton: that.$('.legilo-keywords-btn'),
          result: that.$('.legilo-keywords-view'),
          locationSelectEl: locationSelect,
          onSelect: keywordOnSelect
        })
        if (that.data.renderLegiloKeywords) {
          that.listenTo(that.model, 'change:' + that.legiloKeywords.fetcher.name, that.legiloKeywords.render.bind(that.legiloKeywords))
        }
      }
    })
  },

  render () {
    this.$el.html(this.template({ data: this.data }))
    return this
  },

  addOne (model) {
    const view = new ChildView(_.extend({}, this.data, { model, parentModel: this.model }))
    const that = this
    $(document).ready(function () {
      that.$('.existing').append(view.el)
      if (that.data.disabled !== 'disabled' && that.data.modelAttribute !== 'boundingBoxes') {
        that.$('.existing').sortable({
          animation: 150,
          start: (event, ui) => {
            that._oldPosition = ui.item.index()
          },
          update: (event, ui) => {
            that.collection.position(that._oldPosition, ui.item.index())
          }
        })
      }
    })
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
    let data = this.collection.toJSON()

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
