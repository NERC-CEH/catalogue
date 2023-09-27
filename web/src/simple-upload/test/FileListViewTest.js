import $ from 'jquery'
import Backbone from 'backbone'
import { FileListView, FileCollection } from '../src/File'

describe('Test FileListView', function () {
  const template =
    `<div> 
      <div id="files-tools"></div> 
      <ul id="files-list"></ul> 
    </div>`
  let el = null
  let files = null
  let messages = null
  let view = null

  beforeEach(function () {
    el = $(template).appendTo($('body'))
    files = new FileCollection({ url: '/upload/test' })
    messages = new Backbone.Collection()
    view = new FileListView({
      el,
      files,
      messages
    })
    spyOn(view, 'addOne').and.callThrough()
    spyOn(view, 'addAll').and.callThrough()
    spyOn(view, 'selectAll').and.callThrough()
    spyOn(view, 'deleteSelected').and.callThrough()
  })

  afterEach(() => el.remove())

  it('renders buttons', () => {
    expect($('#files-tools button').length).toEqual(2)
  })

  it('file added to collection triggers subview render', function () {
    // when
    files.add({ name: 'test.csv', urlEncodedName: 'test.csv' })

    // then
    expect($('#files-list li').length).toEqual(1)
    expect(view.addOne).toHaveBeenCalled()
  })

  it('resetting collection triggers subview render', function () {
    // when
    files.reset([
      { name: 'test0.csv', urlEncodedName: 'test0.csv' },
      { name: 'test1.csv', urlEncodedName: 'test1.csv' },
      { name: 'test2.csv', urlEncodedName: 'test2.csv' }
    ])

    // then
    expect($('#files-list li').length).toEqual(3)
    expect(view.addAll).toHaveBeenCalled()
  })
})
