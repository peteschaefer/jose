#!/bin/bash

cd $(dirname "$0")/../..

vmargs=-Djava.library.path=lib/Mac
args=jose.db=MySQL-standalone

jre/bin/java $vmargs -jar jose.jar $args

# note: use shc to create an binary executable from this script
