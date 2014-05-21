# Vagrant up file, call this when you want to start your vagrant box
# and get it to populate the modules ready for testing
vagrant up

# Set up the grunt projects ready for working on
cd ukeof_code/javascript-modules/src/main
npm install
grunt prep
grunt build