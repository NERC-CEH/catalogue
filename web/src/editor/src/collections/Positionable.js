import Backbone from 'backbone'

export default Backbone.Collection.extend({
  /*
     * Moves an existing element in the collection from position index
     * to newPosition. Any "position" listeners of this instance will be
     * notified with the arguments:
     *     model - the model which moved
     *     collection - this Layers instance
     *     newPosition - the new position of the model
     *     oldPosition - the position the model was in
     */
  position (index, newPosition) {
    const toMove = (this.models.splice(index, 1))[0]
    this.models.splice(newPosition, 0, toMove)
    this.trigger('position', toMove, this, newPosition, index)
  }
})
