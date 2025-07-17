# Overview

The project is still very much a work in progress, the following outline is meant to give a general idea of the original intentions, but most of the actual namespaces probably don't fit that original intention.

## api

Originally, just clojure wrappers around the jbwapi api. Mostly not in use anymore and will eventually be deleted.

The only thing still in use is `client`.

## game

Where I'm trying to map the domain of the game itself.

## htn

Hierarchical task network planning

## procs

core.async.flow procs I experimented with

## systems

Mostly stateful components. Of note here are `async-bot`, `flow-bot`, and `sync-bot`. After having rewritten multiple times the "core" of the bot as I experimented with trying to make it bootable and controllable from the REPL, I started preserving those previous attempts in those files, both for reference and as a way to keep track of what I've tried.

I did try a couple of other things, but they either got completely rewritten but can still be found in the commit history, or I never comitted them, as during the first 1-2 months of development, I did not use version control in the project (lmao)

## utils

Miscellaneous utilities. Important here is `chaoslauncher`, which starts and kills the chaoslauncher.exe

## config

Parses the config file

## core

The core of the program

## interop

An attempt/experiment at data-driven interop, inspired by Replicant (and the newer Nexus), aws-api, ring, etc.

## pro

What would be the main entry point for the bot in a "production" environment

## vpd

Another attempt/experiment at data-driven interop, inspired by [Ooloi](https://github.com/PeterBengtson/Ooloi-docs)
