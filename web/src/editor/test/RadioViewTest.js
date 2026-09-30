import { EditorMetadata } from '../src'
import { RadioView } from '../src/views'

describe('Test RadioView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new EditorMetadata({ title: 'some text' })
  })

  it('when view is constructing should exist', () => {
    view = new RadioView({
      model,
      modelAttribute: 'testBoolean'
    })
    expect(view).toBeDefined()
  })

  it('renders', () => {
    view = new RadioView({
      model,
      modelAttribute: 'testBoolean'
    })
    view.render()
    expect(view.$('input')).toBeDefined()
  })

  it('ticks Yes radio when model value is true', () => {
    model.set('testBoolean', true)
    view = new RadioView({
      model,
      modelAttribute: 'testBoolean'
    })
    view.render()
    expect(view.$('#testBoolean-yes').prop('checked')).toBe(true)
    expect(view.$('#testBoolean-no').prop('checked')).toBe(false)
  })

  it('ticks No radio when model value is false', () => {
    model.set('testBoolean', false)
    view = new RadioView({
      model,
      modelAttribute: 'testBoolean'
    })
    view.render()
    expect(view.$('#testBoolean-yes').prop('checked')).toBe(false)
    expect(view.$('#testBoolean-no').prop('checked')).toBe(true)
  })

  it('ticks No radio when model value is null', () => {
    model.set('testBoolean', null)
    view = new RadioView({
      model,
      modelAttribute: 'testBoolean'
    })
    view.render()
    expect(view.$('#testBoolean-yes').prop('checked')).toBe(false)
    expect(view.$('#testBoolean-no').prop('checked')).toBe(true)
  })

  it('ticks No radio when model value is undefined', () => {
    view = new RadioView({
      model,
      modelAttribute: 'testBoolean'
    })
    view.render()
    expect(view.$('#testBoolean-yes').prop('checked')).toBe(false)
    expect(view.$('#testBoolean-no').prop('checked')).toBe(true)
  })

  it('sets model to true when Yes radio is clicked', () => {
    view = new RadioView({
      model,
      modelAttribute: 'testBoolean'
    })
    view.render()
    view.$('#testBoolean-yes').prop('checked', true).trigger('change')
    expect(model.get('testBoolean')).toBe(true)
    expect(typeof model.get('testBoolean')).toBe('boolean')
  })

  it('sets model to false when No radio is clicked', () => {
    view = new RadioView({
      model,
      modelAttribute: 'testBoolean'
    })
    view.render()
    view.$('#testBoolean-no').prop('checked', true).trigger('change')
    expect(model.get('testBoolean')).toBe(false)
    expect(typeof model.get('testBoolean')).toBe('boolean')
  })

  it('sets model to boolean true, not string "true"', () => {
    view = new RadioView({
      model,
      modelAttribute: 'testBoolean'
    })
    view.render()
    view.$('#testBoolean-yes').prop('checked', true).trigger('change')
    expect(model.get('testBoolean')).not.toBe('true')
    expect(model.get('testBoolean')).toBe(true)
  })

  it('sets model to boolean false, not string "false"', () => {
    view = new RadioView({
      model,
      modelAttribute: 'testBoolean'
    })
    view.render()
    view.$('#testBoolean-no').prop('checked', true).trigger('change')
    expect(model.get('testBoolean')).not.toBe('false')
    expect(model.get('testBoolean')).toBe(false)
  })
})
