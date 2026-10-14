import Backbone from 'backbone'
import { EditorMetadata } from '../src'
import { SingleObjectView, ResourceTypeView } from '../src/views'
import { ResourceType } from '../src/models'
import { BoundingBox, BoundingBoxView } from '../src/geometryMap'
import { spy } from '../../test/support'

/**
 * A single-object field (resource type, bounding box, access limitation…) is edited through its own
 * model, but the record keeps plain data: storing the live model made every reader handle two
 * shapes, and leaked a cleared bounding box to the server as nulls (NERC-CEH/dri-one#457).
 */
describe('SingleObjectView', () => {
  function resourceTypeField (record) {
    return new SingleObjectView({
      model: record,
      modelAttribute: 'resourceType',
      ModelType: ResourceType,
      ObjectInputView: ResourceTypeView,
      label: 'Resource type'
    })
  }

  it('stores an edited value on the record as plain data', () => {
    const record = new EditorMetadata({ resourceType: { value: 'dataset' } })
    const view = resourceTypeField(record)

    view.inputModel.set('value', 'service')

    expect(record.get('resourceType') instanceof Backbone.Model).toBe(false)
    // ResourceType also fills in the matching INSPIRE uri
    expect(record.get('resourceType')).toEqual(jasmine.objectContaining({ value: 'service' }))
  })

  it('reports every edit as a change to the record', () => {
    const record = new EditorMetadata({ resourceType: { value: 'dataset' } })
    const view = resourceTypeField(record)
    const changes = spy('change:resourceType')
    record.on('change:resourceType', changes)

    view.inputModel.set('value', 'service')
    view.inputModel.set('value', 'application')

    expect(changes).toHaveBeenCalledTimes(2)
  })

  it('removes a cleared bounding box from the record', () => {
    // As saved: the server adds bounds and coordinates, which used to keep a cleared box alive
    const record = new EditorMetadata({
      boundingBox: { westBoundLongitude: -3, eastBoundLongitude: 1, southBoundLatitude: 50, northBoundLatitude: 55, bounds: '{}', coordinates: '[]' }
    })
    const view = new SingleObjectView({
      model: record,
      modelAttribute: 'boundingBox',
      ModelType: BoundingBox,
      ObjectInputView: BoundingBoxView,
      label: 'Bounding box'
    })

    view.inputModel.clearBounds()

    expect(record.has('boundingBox')).toBe(false)
    expect(JSON.parse(JSON.stringify(record.toJSON())).boundingBox).toBeUndefined()
  })
})
