#!/bin/bash

cd ../..

vmargs=-Djava.library.path=lib/Mac
args=jose.db=MySQL-standalone

jre/bin/java $vmargs -jar jose.jar $args
