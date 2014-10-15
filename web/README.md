[![Built with Grunt](https://cdn.gruntjs.com/builtwith.png)](http://gruntjs.com/)
# CEH Catalogue - Web

This is the Web section to the CEH Catalogue. It is where we manage *JavaScript/CoffeeScript* and *LESS/CSS* stylesheets.

Building this section of the project is handled with [Grunt](http://gruntjs.com/):

1. Install Grunt-Cli

    npm install -g grunt-cli

2. To perform a complete build

    npm install
    grunt build

3. To Start developing

    grunt develop

# Troubleshooting

## ENOENT when npm installing

If when you are installing npm modules for the first time you encounter an error 
like:

    $ npm install 
      Error: ENOENT, stat 'C:\Users\User\AppData\Roaming\npm'

Try to creating an empty directory at the path which is mentioned

# Running npm in babun (or cygwin)

If you are running in cygwin (Babun) you will need to **fix** npm

    dos2unix /cygdrive/c/Program\ Files/nodejs/npm

## Contributing

Built code should be committed to the git repository so that clients only need to checkout the repository and host.
To ensure this happens we perform a git status check in the default grunt task. If any files are uncommitted, this will fail
