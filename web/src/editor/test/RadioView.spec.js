import { EditorMetadata } from '../src'
import { RadioView } from '../src/views'

describe('RadioView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new EditorMetadata({ title: 'Title' })
  })

  describe('construction', () => {
    it('should exist when view is constructing', () => {
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean'
      })
      expect(view).toBeDefined()
    })
  })

  describe('rendering', () => {
    it('should render input elements', () => {
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean'
      })
      view.render()
      expect(view.$('input')).toBeDefined()
    })

    it('should tick Yes radio when model value is true', () => {
      model.set('testBoolean', true)
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean'
      })
      view.render()
      expect(view.$('#testBoolean-yes').prop('checked')).toBe(true)
      expect(view.$('#testBoolean-no').prop('checked')).toBe(false)
    })

    it('should tick No radio when model value is false', () => {
      model.set('testBoolean', false)
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean'
      })
      view.render()
      expect(view.$('#testBoolean-yes').prop('checked')).toBe(false)
      expect(view.$('#testBoolean-no').prop('checked')).toBe(true)
    })

    it('should tick No radio when model value is null', () => {
      model.set('testBoolean', null)
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean'
      })
      view.render()
      expect(view.$('#testBoolean-yes').prop('checked')).toBe(false)
      expect(view.$('#testBoolean-no').prop('checked')).toBe(true)
    })

    it('should tick No radio when model value is undefined', () => {
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean'
      })
      view.render()
      expect(view.$('#testBoolean-yes').prop('checked')).toBe(false)
      expect(view.$('#testBoolean-no').prop('checked')).toBe(true)
    })

    it('should disable inputs when readonly is true', () => {
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean',
        readonly: true
      })
      view.render()
      expect(view.$(':input').prop('disabled')).toBe(true)
    })
  })

  describe('user interaction', () => {
    it('should set model to true when Yes radio is clicked', () => {
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean'
      })
      view.render()
      view.$('#testBoolean-yes').prop('checked', true).trigger('change')
      expect(model.get('testBoolean')).toBe(true)
      expect(typeof model.get('testBoolean')).toBe('boolean')
    })

    it('should set model to false when No radio is clicked', () => {
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean'
      })
      view.render()
      view.$('#testBoolean-no').prop('checked', true).trigger('change')
      expect(model.get('testBoolean')).toBe(false)
      expect(typeof model.get('testBoolean')).toBe('boolean')
    })

    it('should set model to boolean true, not string "true"', () => {
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean'
      })
      view.render()
      view.$('#testBoolean-yes').prop('checked', true).trigger('change')
      expect(model.get('testBoolean')).not.toBe('true')
      expect(model.get('testBoolean')).toBe(true)
    })

    it('should set model to boolean false, not string "false"', () => {
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

  describe('model changes', () => {
    it('should re-render when model attribute changes', () => {
      view = new RadioView({
        model,
        modelAttribute: 'testBoolean'
      })
      view.render()
      const renderSpy = spyOn(view, 'render').and.callThrough()
      model.set('testBoolean', true)
      expect(renderSpy).toHaveBeenCalled()
    })
  })
})
