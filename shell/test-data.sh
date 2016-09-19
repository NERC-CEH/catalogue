#!/bin/sh
DATASTORE_DIR=datastore

# Create an empty git repository in the DATASTORE_DIR
rm -Rf $DATASTORE_DIR
mkdir -p $DATASTORE_DIR

cd $DATASTORE_DIR
git init
git config user.name "Vagrant provision"
git config user.email vagrant@localhost

for d in ../fixtures/datastore/*/ ; do
  cp ${d}* .
  git add -A
  git commit -m "Adding ${d} via test script"
done
