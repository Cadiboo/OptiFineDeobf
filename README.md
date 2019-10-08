# [OptiFineDeobf](https://github.com/Cadiboo/OptiFineDeobf)
OptiFineDeobf is a tool that deobfuscates individual class files and "extracted"/"mod" OptiFine jars.
It can be used to remap the method and field names in any jar or class file.
Its primary function is to deobfuscate & tweak an srg named OptiFine mod for use in a 1.13+ development environment.
It was made because both [simpledeobf](https://github.com/octarine-noise/simpledeobf) and [BON2](https://github.com/tterrag1098/BON2) were unable to deobfuscate OptiFine due to both tools not remapping `INVOKEDYNAMIC` instruction arguments and BON2's inability to deal with OptiFine's classes in the `srg/` folder.

## Usage
#### Deobfuscating a single class file
1) Select (Drag & Drop is supported) the input class file
2) Optionally choose a non-default mappings file
3) Choose your options
4) Deobf the class file
#### Deobfuscating an OptiFine jar
1) Open the OptiFine installer and "extract" OptiFine
2) Select the extracted OptiFine jar as the input for OptiFineDeobf
3) Optionally choose a non-default mappings file
4) Choose your options
    - `Remap file names` saves class files under their class names, rather than in their original locations. In practice it means that files in OptiFine's `srg/` folder are saved in the root directory. i.e. `srg/net/optifine/Config.class` becomes `net/optifine/Config.class` because the class file's package declaration is `net.optifine`.
    - `Forge Dev Jar` applies the following tweaks to the OptiFine jar to get it to load in a development environment:
        - Discards Forge dummy classes (everything from `net/minecraftforge/` in the OptiFine jar)
        - Discards all `javax` dummy classes (everything from `javax/` in the OptiFine jar)
        - Discards all patched obfuscated/notch-named minecraft classes (all class files (except `Config.class`) from the root directory in the OptiFine jar)
        - Discards all OptiFine classes compiled against obfuscated/notch-named minecraft classes (all classes with counterparts in `srg/`)
        - Injects a dummy OptiFine mod class to stop loading errors in dev
        - Duplicates all classes from `srg/` to both `srg/` and the root directory (This overwrites the classes compiled against obfuscated/notch-named minecraft classes in the root directory)
4) Deobf the OptiFine jar
## [Downloads](https://github.com/Cadiboo/OptiFineDeobf/releases)
Download the latest release [here](https://github.com/Cadiboo/OptiFineDeobf/releases/latest)
