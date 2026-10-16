// The micro-apps' code, each fetched by import() only on a page that has the app, as chunks of its own:
// a public record page doesn't download the editor, the maps or the upload tables (NERC-CEH/dri-one#364).
// They're one object so that the start-up specs can replace a loader with spyOn rather than load the
// chunk (dri-one #473). Each resolves to the classes its app uses.
export const load = {
  editor: () => Promise.all([
    import('../editor/src/editors'),
    import('../editor/src'),
    import('../editor/src/models')
  ]).then(([editors, src, models]) => ({ ...editors, ...src, ...models })),

  catalogueApp: () => import('../catalogue/src/CatalogueApp'),

  studyArea: () => import('../study-area/src/View'),

  messageView: () => import('../search/src/views/MessageView')
    .then(module => module.default),

  swal: () => import('sweetalert2').then(module => module.default),

  permission: () => import('../permission/src/PermissionApp'),

  search: () => Promise.all([
    import('../search/src/SearchApp'),
    import('../search/src/SearchAppView'),
    import('../search/src/SearchRouter')
  ]).then(([app, view, router]) => ({ SearchApp: app.default, SearchAppView: view.default, SearchRouter: router.default })),

  searchRouter: () => import('../search/src/SearchRouter').then(module => module.default),

  simpleUpload: () => import('../simple-upload/src/App'),

  hubbub: () => import('../hubbub/src/Upload'),

  metrics: () => import('../metrics/src'),

  depositRequest: () => Promise.all([
    import('../deposit-request/src/model/DepositRequestApp'),
    import('../deposit-request/src/view/DepositRequestFormView')
  ]).then(([app, view]) => ({ DepositRequestApp: app.default, DepositRequestFormView: view.default }))
}
