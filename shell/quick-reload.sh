#!/bin/sh
vagrant ssh -c "sudo -- sh -c 'cd /opt/ceh-catalogue/shell; ./test-data.sh; ./resume.sh'"
