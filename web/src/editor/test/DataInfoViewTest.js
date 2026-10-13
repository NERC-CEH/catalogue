import $ from 'jquery'
import Backbone from 'backbone'
import { DataInfoView } from '../src/views'
import { CehModelApplicationEditorView } from '../src/editors'
import { EditorMetadata } from '../src'

/**
 * Input and output data rows are shared by two editors whose server classes name the parameter
 * differently: NERC model use has `name`, CEH model application has `variableName`. The shared row
 * bound `name` for both from 2023-02, so a CEH model application dropped every parameter name on
 * save (NERC-CEH/dri-one#455).
 */
describe('DataInfoView', () => {
  function nameInput (view) {
    return view.$('input').first()
  }

  it('binds name by default (NERC model use)', () => {
    const model = new Backbone.Model({ name: 'rainfall' })
    const view = new DataInfoView({ model, index: 'c1' })

    expect(nameInput(view).val()).toBe('rainfall')
    expect(nameInput(view).data('name')).toBe('name')
  })

  it('binds the attribute it is given', () => {
    const model = new Backbone.Model({ variableName: 'rainfall' })
    const view = new DataInfoView({ model, index: 'c1', nameAttribute: 'variableName' })

    expect(nameInput(view).val()).toBe('rainfall')
    nameInput(view).val('runoff').trigger('change')
    expect(model.get('variableName')).toBe('runoff')
    expect(model.has('name')).toBe(false)
  })

  it('shows a CEH model application\'s stored variable names in the editor', () => {
    const editor = new CehModelApplicationEditorView({
      model: new EditorMetadata({ inputData: [{ variableName: 'rainfall' }], outputData: [{ variableName: 'runoff' }] })
    })

    const values = editor.$('[data-name="variableName"]').map((i, el) => $(el).val()).get()
    expect(values).toEqual(['rainfall', 'runoff'])
    $('body').removeClass('edit-mode')
  })
})
