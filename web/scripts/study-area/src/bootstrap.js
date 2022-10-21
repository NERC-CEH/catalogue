
import { StudyAreaView } from './View/index'

if ($('#studyarea-map').length) { initStudyArea() }

function initStudyArea () {
  // eslint-disable-next-line no-unused-vars
  const view = new StudyAreaView({ el: '#studyarea-map' })
}
