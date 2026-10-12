import _ from 'underscore'
import SingleView from '../SingleView'

export default SingleView.extend({

  initialize (options) {
    SingleView.prototype.initialize.call(this, options)
    this.render()

    this.inputModel = new this.data.ModelType(this.model.get(this.data.modelAttribute))
    this.listenTo(this.inputModel, 'change', this.updateMetadataModel)
    this.listenTo(this.model, 'sync', function (model) {
      this.inputModel.set(model.get(this.data.modelAttribute))
    })
    /* eslint no-new: "off" */
    this.objectInputView = new this.data.ObjectInputView(_.extend({}, this.data, {
      el: this.$('.dataentry'),
      model: this.inputModel
    }))
  },

  show () {
    SingleView.prototype.show.apply(this)
    if (this.data.modelAttribute === 'boundingBox') {
      this.objectInputView.viewMap()
    }
  }
})
