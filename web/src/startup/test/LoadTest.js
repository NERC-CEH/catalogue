import { load } from '../load'

// Each loader fetches its micro-app's chunk and resolves to the classes its start-up uses: a renamed
// export or moved file shows up here rather than as a page that silently never starts (dri-one #473)
describe('Micro-app loaders', () => {
  const EXPECTED = {
    editor: ['GeminiEditorView', 'EditorMetadata', 'ServiceAgreementEditorView', 'ServiceAgreement'],
    catalogueApp: ['Catalogue', 'CatalogueView', 'CatalogueViewModel', 'CatalogueViewView', 'linkHref'],
    studyArea: ['StudyAreaView'],
    permission: ['PermissionApp', 'PermissionAppView', 'PermissionRouter'],
    search: ['SearchApp', 'SearchAppView', 'SearchRouter'],
    simpleUpload: ['SimpleUploadView'],
    hubbub: ['UploadModel', 'UploadView'],
    metrics: ['MetricsReportApp', 'MetricsReportForm', 'MetricsReportResults'],
    depositRequest: ['DepositRequestApp', 'DepositRequestFormView']
  }

  Object.entries(EXPECTED).forEach(([name, classes]) => {
    it(`${name} gives ${classes.join(', ')}`, async () => {
      const module = await load[name]()
      classes.forEach(each => expect(module[each]).withContext(each).toEqual(jasmine.any(Function)))
    })
  })

  it('messageView, searchRouter and swal give what their callers use directly', async () => {
    expect(await load.messageView()).toEqual(jasmine.any(Function))
    expect(await load.searchRouter()).toEqual(jasmine.any(Function))
    expect((await load.swal()).fire).toEqual(jasmine.any(Function))
  })

  it('covers every loader', () => {
    expect(Object.keys(load).sort()).toEqual([...Object.keys(EXPECTED), 'messageView', 'searchRouter', 'swal'].sort())
  })
})
