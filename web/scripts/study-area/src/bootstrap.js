import $ from 'jquery'
import { StudyAreaView } from './View/index'

if ($('#studyarea-map').length) { initStudyArea() }

function initStudyArea () {
  const view = new StudyAreaView({ el: '#studyarea-map' })
}
