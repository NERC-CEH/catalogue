import _ from 'underscore'
import $ from 'jquery'
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
    this.objectInputView = null
    const that = this
    $(document).ready(function () {
      /* eslint no-new: "off" */
      that.objectInputView = new that.data.ObjectInputView(_.extend({}, that.data, {
        el: that.$('.dataentry'),
        model: that.inputModel
      }))
    })
  }
})
