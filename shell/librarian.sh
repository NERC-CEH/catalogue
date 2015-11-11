#!/bin/sh

# Directory in which librarian-puppet should manage its modules directory
PUPPET_DIR=/etc/puppet/

# NB: librarian-puppet might need git installed. If it is not already installed
# in your basebox, this will manually install it at this point using apt or yum
apt-get -q -y update
apt-get -q -y install git
apt-get -q -y install ruby1.9.1-dev

if [ ! -d "$PUPPET_DIR" ]; then
  mkdir -p $PUPPET_DIR
fi
cp /opt/ceh-catalogue/Puppetfile $PUPPET_DIR

if [ "$(gem list -i '^librarian-puppet$')" = "false" ]; then
  gem install librarian-puppet -v '2.2.1'
  cd $PUPPET_DIR && librarian-puppet install --clean
else
  cd $PUPPET_DIR && librarian-puppet update
fi
