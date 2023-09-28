import _ from 'underscore'
import SingleView from '../../SingleView'

export default SingleView.extend({

  className: 'component component--textonly',

  initialize (options) {
    this.template = _.template('<%= data.text %>')
    SingleView.prototype.initialize.call(this, options)
    this.render()
  },

  render () {
    SingleView.prototype.render.apply(this)
    this.$('.dataentry').append(this.template({ data: _.extend({}, this.data, { value: this.model.get }) }))
    return this
  }
})
