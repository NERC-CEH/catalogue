import $ from 'jquery'
import * as editors from '../src/editors'
import { EditorMetadata } from '../src'

/**
 * Element ids in the editor must be unique on the page, and every `<label for>` must name exactly one
 * element. Repeated ids made a label in one row focus a field in another (or a hidden one), and gave
 * screen readers the wrong label. See NERC-CEH/dri-one#452.
 */
describe('Editor element ids', () => {
  afterEach(() => {
    $('body').removeClass('edit-mode')
    $('#search').removeClass('search')
  })

  // Every repeating section gets two rows, so ids that only differ by row must differ
  function buildWithRows (Editor) {
    const editor = new Editor({ model: new EditorMetadata({ type: 'dataset' }) })
    editor.sections
      .flatMap(section => section.views)
      .filter(view => view.collection && view.data && view.data.ModelType && typeof view.add === 'function')
      .forEach(view => {
        view.collection.add(new view.data.ModelType())
        view.collection.add(new view.data.ModelType())
      })
    return editor
  }

  function idCounts (editor) {
    const counts = {}
    editor.$('[id]').each((i, el) => { counts[el.id] = (counts[el.id] || 0) + 1 })
    return counts
  }

  Object.entries(editors).forEach(([name, Editor]) => {
    describe(name, () => {
      let editor

      beforeEach(() => {
        editor = buildWithRows(Editor)
      })

      it('gives every element a unique id', () => {
        const repeated = Object.entries(idCounts(editor)).filter(([, n]) => n > 1).map(([id, n]) => `${id} x${n}`)
        expect(repeated).toEqual([])
      })

      it('points every label at exactly one element', () => {
        const counts = idCounts(editor)
        const broken = editor.$('label[for]').toArray()
          .map(label => label.htmlFor)
          .filter(target => counts[target] !== 1)
        expect([...new Set(broken)]).toEqual([])
      })

      it('names every form control with a label', () => {
        // A control with no label is announced as an unnamed field: the service agreement's questions
        // sat in a separate TextOnlyView with nothing tying them to the field (dri-one #462)
        const named = new Set(editor.$('label[for]').toArray().map(label => label.htmlFor))
        const unnamed = editor.$('input[id^="input-"], select[id^="input-"], textarea[id^="input-"]').toArray()
          .filter(control => !named.has(control.id) && !control.hasAttribute('aria-label') && !control.hasAttribute('aria-labelledby'))
          .map(control => control.id)
        expect(unnamed).toEqual([])
      })

      it('points a section label at the section control', () => {
        // The other direction: when a section has a label and renders its input-<modelAttribute>
        // control, the label must name it. A control rendered by a sub-view (the single-object
        // selects) lost its label's for once only views declaring labelsControl kept it.
        const unlinked = editor.sections
          .flatMap(section => section.views)
          .filter(view => view.data && view.data.label && view.data.modelAttribute)
          .filter(view => view.$(`select#input-${view.data.modelAttribute}, input#input-${view.data.modelAttribute}, textarea#input-${view.data.modelAttribute}`).length)
          .filter(view => view.$('.datalabel label, label').first().attr('for') !== `input-${view.data.modelAttribute}`)
          .map(view => view.data.modelAttribute)
        expect(unlinked).toEqual([])
      })
    })
  })
})
