import ObjectInputView from './ObjectInputView'
import template from '../templates/ResourceMaintenance'

export default ObjectInputView.extend({
  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, options)
  },

  events: {
    ...(ObjectInputView.prototype.events || {}),
    'change .frequencyOfUpdate': 'frequencyChanged'
  },

  render () {
    ObjectInputView.prototype.render.apply(this, arguments)

    this.$('.frequencyOfUpdate')
      .val(this.model.get('frequencyOfUpdate'))

    this.frequencyChanged()

    return this
  },

  modify(event) {
      const frequencyOfUpdate = this.$('.frequencyOfUpdate').val();

      this.model.set('frequencyOfUpdate', frequencyOfUpdate);

      this.model.set(
          'note',
          this.$('textarea[data-name="note"]').val()
      );

      this.model.set(
          'lastUpdate',
          this.$('[data-name="lastUpdate"]').val()
      );

      if (!frequencyOfUpdate || frequencyOfUpdate === 'notPlanned') {
          this.model.unset('scope');
      } else {
          this.model.set('scope', {
              attribute: this.$('.scopeAttribute').prop('checked'),
              feature: this.$('.scopeFeature').prop('checked'),
              attributeInstance: this.$('.scopeAttributeInstance').prop('checked'),
              featureInstance: this.$('.scopeFeatureInstance').prop('checked')
          });
      }

      this.frequencyChanged();
      console.log(this.model.get('lastUpdate'));
      console.log(this.model.toJSON());
  },

  frequencyChanged () {
    const value = this.$('.frequencyOfUpdate').val()

    if (value && value !== 'notPlanned') {
      this.$('.scopeRow').removeClass('d-none')
      this.$('.lastUpdateRow').removeClass('d-none')
    } else {
      this.$('.scopeRow').addClass('d-none')
      this.$('.lastUpdateRow').addClass('d-none')
    }
  }
})
