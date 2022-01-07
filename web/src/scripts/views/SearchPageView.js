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
  'jquery',
  'backbone',
  'tpl!templates/SearchPage.tpl',
  'isInViewport'
], function(_, $, Backbone, template) { return Backbone.View.extend({

  initialize() {
    (this.readModelFromHTML)();

    this.listenTo(this.model, 'cleared:results', this.clear);
    this.listenTo(this.model, 'results-sync', this.render);
    this.listenTo(this.model, 'results-change:selected', this.updateSelected);

    (this.findSelected)(); // Find selected, after @updateSelected has been registered
    (this.padResults)();   // Change the padding of the results page

    // Ensure @ refers to this in the findSelected method. This means that we
    // can:
    //   - Pass @findSelected directly to the jquery.on method
    //   - Unbind @findSelected when the view is removed
    _.bindAll(this, 'findSelected', 'padResults');
    $(window).on('scroll', this.findSelected);
    return $(window).on('resize', this.padResults);
  },

  /*
  Since we update the selected result based upon the scroll position, we need
  to be able to scroll all the way to select the last result. This method will
  ensure that the margin of the results view allows for such.
  */
  padResults() {
    if (this.$('.result').length) {
      // Where the top result should start
      const topPosition = $('.navbar').height() + $('.search-form').height();
      const onScreen = this.$('.result').last().nextAll().andSelf(); // Elements to show
      const onScreenHeight = _.reduce(onScreen,
                                (height, e) => height + $(e).outerHeight(true),
                                topPosition);

      return this.$el.css({marginBottom : $(window).height() - onScreenHeight});
    }
  },

  /*
  A page of search results may already be loaded in to the html. We can read
  that html and populate the model. If there is no html this method SHOULD NOT
  trigger a change on the model.
  */
  readModelFromHTML() {
    return this.model.getResults().set({
      numFound: this.$('#num-records').val(),
      results: _.map(this.$('.result'), r => ({
        identifier:  $(r).attr('id'),
        title:       $('.result__title', r).text(),
        description: $('.result__description', r).text(),
        locations:   $(r).attr('data-location').split('|')
      }))
    });
  },

  /*
  Event listener for when the selected id has changed on the search page model.
  Highlight that search result, with the selected class
  */
  updateSelected() {
    const selected = this.model.getResults().get('selected');
    this.$('.result').removeClass('selected');
    return this.$(`#${selected}`).addClass('selected');
  },

  /*
  The following method will identify the result at the top of the screen and
  set the selected property on the @model. If there are no results, do nothing
  */
  findSelected() {
    if (this.$('.result').length) {
      const offset = this.$('.result .result__description').offset().top;
      const results = this.$(`.result:in-viewport(${offset})`);
      // if no result was detected, default to the last result
      const selected = results.length ? $(results[0]) : $('.result').last();
      return this.model.getResults().set({selected: selected.attr('id')});
    }
  },

  /*
  Clear the dom of any content
  */
  clear() { return (this.$el.empty)(); },

  /*
  Draw in the new content
  */
  render() {
    this.$el.html(template(this.model.getResults().attributes));
    const $relatedSearches = this.$('.results__related_searches');
    const relatedSearches = this.model.getResults().get('relatedSearches');
    if (relatedSearches != null) {
      if (relatedSearches.length > 0) {
        $relatedSearches.append('<h3>Related Searches</h3>');
      }

      relatedSearches.forEach(function(relatedSearch, index) {
        let prefix;
        if (index > 0) {
          prefix = ', ';
        } else {
          prefix = '';
        }
        return $relatedSearches.append(
          `${prefix}<a href=\"${relatedSearch.href}\">${relatedSearch.title}</a>`
        );
      });
    }
    (this.findSelected)(); // Find the selected
    return (this.padResults)();
  }
});
 });   // Pad the results pane
