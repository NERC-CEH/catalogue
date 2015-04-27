#!/bin/sh
DATASTORE_DIR=/var/ceh-catalogue/datastore

apt-get -q -y update
apt-get -q -y install git

# Create an empty git repository in the DATASTORE_DIR
rm -Rf $DATASTORE_DIR
mkdir -p $DATASTORE_DIR
cd $DATASTORE_DIR
git init
git config user.name "Vagrant provision"
git config user.email vagrant@localhost

for d in /vagrant/fixtures/*/ ; do
  git rm -rf .  # Remove all index files
  cp ${d}* .
  git add -A
  git commit -m "Adding ${d} via vagrant script"
done