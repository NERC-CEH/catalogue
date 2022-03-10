/* eslint-disable
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
  'underscore',
  'cs!views/editor/ParentStringView',
  'tpl!templates/editor/MultiStringDropdown.tpl'
], function (_, ParentStringView, childTemplate) {
  return ParentStringView.extend({

    /*
    Edit a list of strings, the value of the string comes
    from the options of a dropdown list.
  */

    childTemplate,

    render () {
      (this.renderParent)()
      const $attach = this.$('.existing')
      _.each(this.array, (string, index) => {
        $attach.append(this.childTemplate({
          data: _.extend({}, this.data,
            { index })
        })
        )
        return this.$(`#select${this.data.modelAttribute}${index}`).val(string)
      })
      return this
    }
  })
})
