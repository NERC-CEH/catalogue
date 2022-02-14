/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'backbone',
  'cs!views/upload/simple/FileListView',
  'cs!collections/upload/simple/FileCollection'
], ($, Backbone, FileListView, FileCollection) => describe('FileListView', function() {
  const template =
    `<div> \
<div id="files-tools"></div> \
<ul id="files-list"></ul> \
</div>`;
  let el = null;
  let files = null;
  let messages = null;
  let view = null;

  beforeEach(function() {
    el = $(template).appendTo($('body'));
    spyOn(FileListView.prototype, 'addOne').and.callThrough();
    spyOn(FileListView.prototype, 'addAll').and.callThrough();
    spyOn(FileListView.prototype, 'selectAll').and.callThrough();
    spyOn(FileListView.prototype, 'deleteSelected').and.callThrough();
    files = new FileCollection({
      url: '/upload/test'});
    messages = new Backbone.Collection();
    return view = new FileListView({
      el,
      files,
      messages
    });
  });

  afterEach(() => el.remove());

  it('renders buttons', () => expect($('#files-tools button').length).toEqual(2));

  it('has DOM events', function() {
    expect(view.events['click .delete-selected']).toBeDefined();
    expect(view.events['click .delete-selected']).toEqual('deleteSelected');
    expect(view.events['click .select-all']).toBeDefined();
    return expect(view.events['click .select-all']).toEqual('selectAll');
  });

  it('file added to collection triggers subview render', function() {
    //when
    files.add({name: 'test.csv', urlEncodedName: 'test.csv'});

    //then
    expect($('#files-list li').length).toEqual(1);
    return expect(view.addOne).toHaveBeenCalled();
  });

  return it('resetting collection triggers subview render', function() {
    //when
    files.reset([
      {name: 'test0.csv', urlEncodedName: 'test0.csv'},
      {name: 'test1.csv', urlEncodedName: 'test1.csv'},
      {name: 'test2.csv', urlEncodedName: 'test2.csv'}
    ]);

    //then
    expect($('#files-list li').length).toEqual(3);
    return expect(view.addAll).toHaveBeenCalled();
  });
}));
