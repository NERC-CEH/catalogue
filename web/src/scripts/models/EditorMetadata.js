// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS207: Consider shorter variations of null checks
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone'
], function(_, Backbone) { return Backbone.Model.extend({

  url() {
    return (this.urlRoot)();
  },

  urlRoot() {
    if (this.isNew()) {
      return `/documents?catalogue=${window.location.pathname.split('/')[1]}`;
    } else {
      return `/documents/${this.id}`;
    }
  },

  initialize() {
    if (arguments.length > 1) {
      this.mediaType = arguments[1].mediaType;
      return this.title = arguments[2];
    } else {
      return this.mediaType = 'application/json';
    }
  },

  sync(method, model, options){
    return Backbone.sync.call(this, method, model, _.extend(options, {
      accepts: {
        json: this.mediaType
      },
      contentType: this.mediaType
    }
    )
    );
  },

  validate(attrs) {
    const errors = [];
    if ((attrs != null ? attrs.title : undefined) == null) {
      errors.push('A title is mandatory');
    }

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate because returning something signals a validation error.
      return;
    } else {
      return errors;
    }
  }
});
 });
