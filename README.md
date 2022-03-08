# [OptiFineDeobf](https://github.com/Cadiboo/OptiFineDeobf)
OptiFineDeobf is a tool that deobfuscates individual class files and "extracted"/"mod" OptiFine jars.
It can be used to remap the method and field names in any jar or class file.
Its primary function is to deobfuscate & tweak an srg named OptiFine mod for use in a 1.13+ development environment.
It was made because both [simpledeobf](https://github.com/octarine-noise/simpledeobf) and [BON2](https://github.com/tterrag1098/BON2) were unable to deobfuscate OptiFine due to both tools not remapping `INVOKEDYNAMIC` instruction arguments and BON2's inability to deal with OptiFine's classes in the `srg/` folder.

## [Downloads](https://github.com/Cadiboo/OptiFineDeobf/releases)
Download the latest release [here](https://github.com/Cadiboo/OptiFineDeobf/releases/latest)

## Usage
#### Deobfuscating a single class file
1) Select (Drag & Drop is supported) the input class file
2) Choose a mappings file to use
3) Choose your options
4) Deobf the class file
#### Deobfuscating an OptiFine jar
1) Open the OptiFine installer and "extract" OptiFine
2) Select the extracted OptiFine jar as the input for OptiFineDeobf
3) Choose a mappings file to use
4) Deobf the OptiFine jar

The `Forge Dev Jar` option applies the following tweaks to the OptiFine jar make it useful as a library to compile against in a forge development environment:
  - Discards all classes in `notch/` that would otherwise cause compilation issues (obfuscated classes and stub classes)
  - Changes the paths of all classes in `srg/` to their proper name (e.g. `srg/foo/class1234` -> `foo/SomeClass`)

Note that this only creates a library for you to compile against, this doesn't create an OptiFine that can be put in `mods/` in a dev environment. For that you'll need to use the dev tweaker ([instructions here](https://github.com/Cadiboo/NoCubes#optifine-compatibility))
