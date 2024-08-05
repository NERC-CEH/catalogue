module.exports = function (grunt) {
  grunt.initConfig({
    clean: {
      build: ['css/*']
    },
    decomment: {
      build: {
        options: {
          type: 'text',
          ignore: /url\([\w:/.-]*\)/g // prevents removal of src:url() from css
        },
        files: {
          'css/style-assist.css': 'css/style-assist.css',
          'css/style-ukceh.css': 'css/style-ukceh.css',
          'css/style-cmp.css': 'css/style-cmp.css',
          'css/style-datalabs.css': 'css/style-datalabs.css',
          'css/style-edge.css': 'css/style-edge.css',
          'css/style-eidc.css': 'css/style-eidc.css',
          'css/style-elter.css': 'css/style-elter.css',
          'css/style-erammp.css': 'css/style-erammp.css',
          'css/style-inlicensed.css': 'css/style-inlicensed.css',
          'css/style-inms.css': 'css/style-inms.css',
          'css/style-nc.css': 'css/style-nc.css',
          'css/style-nm.css': 'css/style-nm.css',
          'css/style-m.css': 'css/style-m.css',
          'css/style-osdp.css': 'css/style-osdp.css',
          'css/style-pimfe.css': 'css/style-pimfe.css',
          'css/style-infrastructure.css': 'css/style-infrastructure.css',
          'css/style-sa.css': 'css/style-sa.css',
          'css/style-ukeof.css': 'css/style-ukeof.css',
          'css/style-ukscape.css': 'css/style-ukscape.css'
        }
      }
    },
    less: {
      options: {
        compress: true
      },
      build: {
        files: [
          { src: 'less/style-assist.less', dest: 'css/style-assist.css' },
          { src: 'less/style-ukceh.less', dest: 'css/style-ukceh.css' },
          { src: 'less/style-cmp.less', dest: 'css/style-cmp.css' },
          { src: 'less/style-datalabs.less', dest: 'css/style-datalabs.css' },
          { src: 'less/style-edge.less', dest: 'css/style-edge.css' },
          { src: 'less/style-eidc.less', dest: 'css/style-eidc.css' },
          { src: 'less/style-elter.less', dest: 'css/style-elter.css' },
          { src: 'less/style-erammp.less', dest: 'css/style-erammp.css' },
          { src: 'less/style-inlicensed.less', dest: 'css/style-inlicensed.css' },
          { src: 'less/style-inms.less', dest: 'css/style-inms.css' },
          { src: 'less/style-nc.less', dest: 'css/style-nc.css' },
          { src: 'less/style-nm.less', dest: 'css/style-nm.css' },
          { src: 'less/style-m.less', dest: 'css/style-m.css' },
          { src: 'less/style-osdp.less', dest: 'css/style-osdp.css' },
          { src: 'less/style-pimfe.less', dest: 'css/style-pimfe.css' },
          { src: 'less/style-infrastructure.less', dest: 'css/style-infrastructure.css' },
          { src: 'less/style-sa.less', dest: 'css/style-sa.css' },
          { src: 'less/style-ukeof.less', dest: 'css/style-ukeof.css' },
          { src: 'less/style-ukscape.less', dest: 'css/style-ukscape.css' }
        ]
      }
    },
    watch: {
      less: {
        files: 'less/*',
        tasks: ['less']
      }
    },
    concurrent: {
      watch: {
        tasks: ['watch:less'],
        options: {
          logConcurrentOutput: true
        }
      }
    }
  })
  grunt.loadNpmTasks('grunt-concurrent')
  grunt.loadNpmTasks('grunt-contrib-clean')
  grunt.loadNpmTasks('grunt-contrib-less')
  grunt.loadNpmTasks('grunt-contrib-watch')
  grunt.loadNpmTasks('grunt-decomment')
  grunt.registerTask('develop', ['less', 'concurrent'])
  grunt.registerTask('build', ['clean', 'less', 'decomment'])
  grunt.registerTask('default', ['build'])
}
