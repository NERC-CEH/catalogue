import _ from 'underscore'
import $ from 'jquery'
import SingleView from '../SingleView'
import ChildView from './ChildView'
import template from '../templates/Parent'
import { Positionable } from '../collections'
import { LegiloKeywords, fetchKeywordsFromLegilo } from './index'
import FetchKeywordsButton from '../templates/FetchKeywordsButton'

export default SingleView.extend({

  events: {
    'click button.add': 'add',
    'click .legilo-fetch-btn': 'fetchKeywords'
  },

  initialize (options) {
    if (typeof this.template === 'undefined') {
      this.template = template
    }
    SingleView.prototype.initialize.call(this, options)
    this.collection = new Positionable([], { model: this.data.ModelType })

    if (this.data.renderLegiloKeywords && this.model.get('id')) {
      this.legiloKeywords = new LegiloKeywords({
        collection: this.collection,
        model: this.model
      })
    }

    this.listenTo(this.collection, 'add', this.addOne)
    this.listenTo(this.collection, 'reset', this.addAll)
    this.listenTo(this.collection, 'add remove change position', this.updateModel)
    this.listenTo(this.model, 'sync', this.updateCollection)

    if (this.data.renderLegiloKeywords) {
      this.listenTo(this.model, 'change:fetchedKeywords', this.renderKeywords)
    }

    this.render()
    this.collection.reset(this.getModelData())

    if (this.data.multiline) {
      this.$el.addClass('multiline')
    }
  },

  render () {
    this.$el.html(this.template({ data: this.data }))

    if (this.data.fetchKeywordsButton && this.model.get('id')) {
      this.$el.append(FetchKeywordsButton())
    }

    if (this.data.renderLegiloKeywords && this.model.get('id')) {
      this.$el.append(this.legiloKeywords.el)
      this.legiloKeywords.render()
      this.legiloKeywords.delegateEvents()
    }
    return this
  },

  fetchKeywords () {
    this.$('.loader').show()

    fetchKeywordsFromLegilo(this.model)
      .then(keywords => {
        this.model.set('fetchedKeywords', keywords)
        this.renderKeywords()

        this.$('.loader').hide()
      })
      .catch(error => {
        console.error('Error fetching keywords:', error)
      })
  },

  renderKeywords () {
    if (this.legiloKeywords) {
      const keywords = this.model.get('fetchedKeywords')
      this.legiloKeywords.renderKeywords(keywords)
    }
  },

  addOne (model) {
    const view = new ChildView(_.extend({}, this.data, { model }))
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
