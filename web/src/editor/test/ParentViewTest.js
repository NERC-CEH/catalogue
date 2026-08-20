import Backbone from 'backbone'
import ParentView from '../src/views/ParentView'
import ParentLargeView from '../src/views/ParentLargeView'
import { hasContent } from '../src/utils'

/**
 * ParentView drives every repeating-row field in every editor (109 call sites, 40+ model
 * attributes). Clicking its add button creates a row model and immediately syncs the collection into
 * the parent model, so a row that is added and never filled in used to be written to the record as
 * an empty object - which then rendered as a stray separator or an empty labelled row on the record
 * page. See NERC-CEH/dri-one#297.
 */
const StubInputView = Backbone.View.extend({})

// `disabled` skips the sortable() wiring, which needs a real DOM layout.
const build = (View, attrs, options = {}) => {
  const model = new Backbone.Model(attrs)
  const view = new View(Object.assign({
    model,
    ObjectInputView: StubInputView,
    disabled: 'disabled'
  }, options))
  return { model, view }
}

describe('hasContent', () => {
  it('treats an attribute-less row as empty', () => {
    expect(hasContent({})).toBeFalse()
  })

  it('treats blank and whitespace-only strings as empty', () => {
    expect(hasContent({ value: '', uri: '' })).toBeFalse()
    expect(hasContent({ value: '   ' })).toBeFalse()
  })

  it('treats null and undefined as empty', () => {
    expect(hasContent({ value: null, uri: undefined })).toBeFalse()
  })

  /**
   * The dangerous case. boundingBoxes is one of the fields ParentView drives, and a box of all
   * zeroes is a real location in the Gulf of Guinea - a falsy check would silently delete it.
   */
  it('treats the number zero as content', () => {
    expect(hasContent({
      northBoundLatitude: 0,
      southBoundLatitude: 0,
      eastBoundLongitude: 0,
      westBoundLongitude: 0
    })).toBeTrue()
    expect(hasContent({ value: '0' })).toBeTrue()
  })

  it('treats false as content', () => {
    expect(hasContent({ published: false })).toBeTrue()
  })

  it('recurses into nested structures', () => {
    expect(hasContent({ observedProperty: [] })).toBeFalse()
    expect(hasContent({ observedProperty: [{ value: '' }] })).toBeFalse()
    expect(hasContent({ observedProperty: [{ value: 'rainfall' }] })).toBeTrue()
  })

  it('keeps a row that has any populated field', () => {
    expect(hasContent({ value: 'flood', uri: '' })).toBeTrue()
    expect(hasContent({ value: '', uri: 'http://example.com/flood' })).toBeTrue()
  })
})

describe('ParentView empty rows', () => {
  it('does not write a newly added empty row into the model', () => {
    const { model, view } = build(ParentView, { keywordsOther: [] }, {
      modelAttribute: 'keywordsOther'
    })

    view.add()

    expect(model.get('keywordsOther')).toEqual([])
  })

  it('still shows the added row in the form so it can be typed into', () => {
    const { view } = build(ParentView, { keywordsOther: [] }, {
      modelAttribute: 'keywordsOther'
    })

    view.add()

    expect(view.collection.length).toBe(1)
  })

  it('writes the row to the model as soon as it is filled in', () => {
    const { model, view } = build(ParentView, { keywordsOther: [] }, {
      modelAttribute: 'keywordsOther'
    })

    view.add()
    view.collection.at(0).set('value', 'flood')

    expect(model.get('keywordsOther')).toEqual([{ value: 'flood' }])
  })

  it('keeps populated rows and drops only the empty one', () => {
    const { model, view } = build(ParentView, {
      keywordsOther: [{ value: 'flood' }, { value: 'drought' }]
    }, { modelAttribute: 'keywordsOther' })

    view.add()

    expect(model.get('keywordsOther')).toEqual([{ value: 'flood' }, { value: 'drought' }])
  })

  it('does not drop a bounding box whose coordinates are all zero', () => {
    const box = {
      northBoundLatitude: 0,
      southBoundLatitude: 0,
      eastBoundLongitude: 0,
      westBoundLongitude: 0
    }
    const { model, view } = build(ParentView, { boundingBoxes: [box] }, {
      modelAttribute: 'boundingBoxes'
    })

    view.updateModel()

    expect(model.get('boundingBoxes')).toEqual([box])
  })

  /**
   * updateCollection runs on every save. Filtering leaves the saved attribute deep-equal to what it
   * already held, so Backbone records no change, hasChanged is false and updateCollection is skipped
   * - which is what we want: the user's in-progress empty row survives the save in the form without
   * ever being persisted.
   */
  it('keeps a still-empty row in the form when the record syncs back', () => {
    const { model, view } = build(ParentView, {
      keywordsOther: [{ value: 'flood' }]
    }, { modelAttribute: 'keywordsOther' })

    view.add()
    expect(view.collection.length).toBe(2)

    model.trigger('sync', model)

    expect(view.collection.length).toBe(2)
    expect(model.get('keywordsOther')).toEqual([{ value: 'flood' }])
  })

  /**
   * A consequence worth having: because the filtered value is deep-equal to the existing one,
   * clicking add no longer registers a model change, so it no longer marks the editor dirty.
   */
  it('does not mark the model changed when an empty row is added', () => {
    const { model, view } = build(ParentView, {
      keywordsOther: [{ value: 'flood' }]
    }, { modelAttribute: 'keywordsOther' })

    view.add()

    expect(model.hasChanged('keywordsOther')).toBeFalse()
  })

  it('writes through a nested model attribute path unchanged', () => {
    const { model, view } = build(ParentView, {
      mapDataDefinition: { data: [{ value: 'layer' }] }
    }, { modelAttribute: 'mapDataDefinition.data' })

    view.add()

    expect(model.get('mapDataDefinition')).toEqual({ data: [{ value: 'layer' }] })
  })
})

describe('ParentLargeView empty rows', () => {
  it('does not write a newly added empty row into the model', () => {
    const { model, view } = build(ParentLargeView, { fileset: [] }, {
      modelAttribute: 'fileset'
    })

    view.add()

    expect(model.get('fileset')).toEqual([])
  })

  it('keeps populated rows', () => {
    const { model, view } = build(ParentLargeView, {
      fileset: [{ title: 'data.csv' }]
    }, { modelAttribute: 'fileset' })

    view.add()

    expect(model.get('fileset')).toEqual([{ title: 'data.csv' }])
  })
})
