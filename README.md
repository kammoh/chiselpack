# WORK IN PROGRESS!
Trying to figure out:
- how to add inter-project dependencies to subprojects (e.g. chisel depends on local firrlt subproject)
- how to run tests for all subprojects

# chiselpack
Bundle release of Chisel (chisel-lang.org)  and related libraries


Alternative to [chisel-release](https://github.com/ucb-bar/chisel-release), but packing everything into a single package



Published to sonotype.

Add to your project's `build.sbt`:
`libraryDependencies += "xyz.kamyar" %% "chiselpack" % "3.3-SNAPSHOT"`


Includes:
- chisel3
- firrtl
- chisel-testers2
- treadle
- diagrammer

Advantages:
- Add single dependency to start using Chisel, no sub-package compatibility hell
- easy build without the the risk of sbt ending up with wrong package versions





