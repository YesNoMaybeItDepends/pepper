> Sir, we've just had a whole cargo-ship full of whoop-ass dumped on us! We ran into a new strain of Ultralisk and it took a lot o' pepper to bring it down. To top it all off, our recon squad reports that the critter's been reincarnated by a nearby cerebrate and is on its way back for more!

# How to run

1. rename `config.edn` to `config.local.edn`
2. set the absolute path to your chaoslauncher.exe
3. to be documented, check out `dev/pepper/dev.clj`

# BWAPI config

bwapi.ini in starcraft folder

# Overview

The project is still very much a work in progress, the following outline is meant to give a general idea of the original intentions, but most of the actual namespaces probably don't fit that original intention.

## api

Interacting with bwapi

## game

Where I'm trying to map the domain of the game itself.

## htn

Hierarchical task network planning

## utils

Miscellaneous utilities. Important here is `chaoslauncher`, which starts and kills the chaoslauncher.exe
(NOTICE: chaoslauncher was moved to pepper.dev.chaos_launcher)
