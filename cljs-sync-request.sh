#!/usr/bin/env bash
set -eo pipefail
cd $(realpath $(dirname $0))

if [[ ! -f ./project.sh ]]; then
	echo 'Downloading bash helper utilities'
	curl -OL https://raw.githubusercontent.com/jesims/backpack/master/project.sh
fi

source ./project.sh
if [[ $? -ne 0 ]]; then
	exit 1
fi

shadow-cljs () {
	lein trampoline run -m shadow.cljs.devtools.cli $@
}

## build:
## Builds the resources and compiles the source in preparation for deployment
build () {
	clean && \
	shadow-cljs release server
}

## clean:
## Cleans the project of all compiled/generated sources
clean () {
	echo_message 'Cleaning generated folders and files'
	lein clean && \
	rm -rf .shadow-cljs/builds/* .cpcache target/*
}

## deps:
## Installs all necessary dependencies
deps () {
	echo_message 'Installing dependencies'
	npm install && \
	lein deps
}

## repl:
## Starts a node-repl for jacking into
repl () {
	clean
	shadow-cljs node-repl
}

is-ci-platform () {
	[[ -n "$CIRCLECI" ]]
}

## test:
## Runs the ClojureScript unit tests
test () {
	clean
	if is-ci-platform;then
		deps
	fi
	echo_message 'Unit Testing'
	shadow-cljs compile :test --debug --source-maps && \
	node ./target/test/test.js
}

script-invoke $@
