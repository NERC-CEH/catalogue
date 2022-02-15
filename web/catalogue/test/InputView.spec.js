/* eslint-disable
    no-return-assign,
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/editor/InputView',
  'cs!models/EditorMetadata'
], (InputView, EditorMetadata) => describe('InputView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new EditorMetadata({ title: 'some text' })
    return view = new InputView({ model })
  })

  return describe('when view is constructing', () => it('should exist', () => expect(view).toBeDefined()))
}))
