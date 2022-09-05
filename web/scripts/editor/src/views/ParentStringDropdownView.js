import _ from 'underscore'
import ParentStringView from './ParentStringView'
import childTemplate from '../templates/MultiStringDropdown.tpl'

export default ParentStringView.extend({

  /*
    Edit a list of strings, the value of the string comes
    from the options of a dropdown list.
  */

  render () {
    this.template = _.template(childTemplate)
    ParentStringView.render()
    _.each(this.array, (string, index) => {
      this.$('.existing').append(this.childTemplate({
        data: _.extend({}, this.data,
          { index })
      })
      )
      return this.$(`#select${this.data.modelAttribute}${index}`).val(string)
    })
    return this
  }
})
