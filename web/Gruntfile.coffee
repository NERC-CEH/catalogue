module.exports = (grunt) ->
  #Load grunt tasks
  grunt.loadNpmTasks 'grunt-exec'
  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-copy'
  grunt.loadNpmTasks 'grunt-contrib-less'
  grunt.loadNpmTasks 'grunt-contrib-cssmin'
  grunt.loadNpmTasks 'grunt-contrib-watch'

  #Configure tasks
  grunt.initConfig
    exec:
      git_status: 'git diff --exit-code'

    less: 
      development:
        files: 'src/css/style.css' : 'src/less/style.less'

    watch: 
      files: "src/less/*"
      tasks: ["less"]

    clean:
      prep: ['src/css']

    cssmin:
      build:
        src: 'src/css/style.css'
        dest: 'src/css/style.css'

  grunt.registerTask 'prep', ['clean']
  grunt.registerTask 'develop', ['less', 'watch']
  grunt.registerTask 'build', ['clean', 'less', 'cssmin']
  grunt.registerTask 'default', ['prep', 'build', 'exec:git_status']