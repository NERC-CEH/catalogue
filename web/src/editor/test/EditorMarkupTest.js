import $ from 'jquery'
import * as editors from '../src/editors'
import { EditorMetadata } from '../src'

/**
 * The other side of escaping record data (NERC-CEH/dri-one#454): markup that comes from editor
 * configuration (labels, help text, question text) must still render as markup, not show up as
 * literal tags because it was escaped too.
 */
describe('Editor configuration markup', () => {
  afterEach(() => {
    $('body').removeClass('edit-mode')
    $('#search').removeClass('search')
  })

  Object.entries(editors).forEach(([name, Editor]) => {
    it(`${name} shows no markup as text`, () => {
      const editor = new Editor({ model: new EditorMetadata({ type: 'dataset' }) })

      // Real HTML element tags only: help text also shows placeholders such as <REGION> on purpose
      const literalTags = (editor.$el.text().match(/<\/?(a|b|br|code|div|em|h[1-6]|i|li|ol|p|small|span|strong|u|ul)(\s[^<>]*)?\/?>|&(lt|gt|amp|quot);/gi) || [])
      expect(literalTags).toEqual([])
    })
  })
})
