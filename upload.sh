#!/bin/bash

spacewalk_uploader.sh -s http://spacewalk.flegler.com/ -u $SPACEWALK_USER -P $SPACEWALK_PASSWORD -c flegler_64bit -l flegler_32bit -z $WORKSPACE/target/rpm/pymon/RPMS/noarch/*rpm
