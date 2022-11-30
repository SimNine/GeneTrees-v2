<!-- markdownlint-disable-file MD024 -->

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

This project **does not** adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This is a branch of GeneTrees-v1, forked after its 0.4.0 tag. Where GeneTrees-v1 simulated only one tree at a time, GeneTrees-v2 simulates many trees at once, thus allowing interaction between trees.

## [Unreleased]
- Nothing yet

## [0.8.0] - 2022-11-30

### Added
- Parameter configuration file
	- Includes configuration settings for all environment, mutation, and fitness parameters
	
### Removed
- Vestigial (broken) functionality of saver/loader

## [0.7.0] - 2022-08-05

### Added
- Multi-tabbed graph display
	- "Population" panel, showing the total tree population per generation
	- "Node stats" panel, showing the average number of each type of node per generation
	- "Particles" panel, showing the total number of each type of particle created per generation
	- "Performance" panel, showing the average computation time per generation
- Deterministic simulation generation and reproducibility through Java.Random seeding
- Javadocs on some heavily-used methods and classes
- Static class "EnvironmentParameters" containing nearly all environment, mutation, reproduction, and physics constants which can be tuned for fine-grained simulation control, such as:
	- Environment seed
	- Environment dimensions
	- Environment ground elevation
	- Number of ticks per generation
	- RainDrop/SunSpeck base power and power decay
	- Root node nutrient collection per size
	- Node fitness decay per size
	- Node minimum size and minimum distance from parent
	- Node chance to add/lose a child
	- ...and many more
- Option to generate an image of the entire environment to a directory at the end of each generation
- Key to trigger a single tick
- Two new data series to "Firness" graph panel
	- Average nutrients (root node resource) per tree
	- Average energy (leaf/raincatcher node resource) per tree

### Changed
- Main display is now pannable
- Restructured package. Now follows correct Java package naming conventions
- Simulation logic abstracted out of GeneTreePanel class, into Simulation class
- Environment state abstracted out of GeneTreePanel class, into Environment class
- Abstracted common features of SunSpeck/RainDrop into "Particle" class
- Redid multithreading; it now divides the set of particles into equal-sized sets, and each thread computes collision for the particles in its subset
- Node coordinates now refer to the top-left corner of the node, rather than the center of the node
- Greatly improved particle collision detection by immediately skipping any trees that do not intersect with a particle, rather than checking each node anyway
- Changed child tree mutation to be chance-based, rather than guaranteed
- Changed root nodes to calculate fitness based on root depth and size, rather than size alone
- Improved caching of GeneTree's nodes by making it unnecessary to recurse through all nodes each time
- Changed reproduction to be based on surplus fitness alone, rather than fitness percentile
	- It costs a certain amount of fitness per parent node to produce a child
	- As many children as possible will be created given the parent's fitness
- Changed CHANGELOG.md and README.md to markdown

### Removed
- Tick timer and constrained tick speed. Ticks now happen as fast as possible by default
- 'build' folder with outdated binary

### Fixed
- Particles being absorbed by more than one tree if they collide with more than one tree in a single tick. Added "isConsumed" check to particle collision detection
- Highlighting bounds of selected trees
- Highlighting nodes that are being hovered over while debug is on
- Incorrect removal of energy from a tree when it would catch a particle with negative energy; negative energy particles are now ignored

### Broke
- Saving/loading

## [0.6.0] - 2018-04-30

### Added
- Two more graphs:
	- Average number of each type of node
	- Number of each type of particle
- Persistence of graphs between simulations
- Seed count on particle graph

## [0.5.0] - 2017-11-03

### Added
- Graphing module; moved it to [UrfJavaUtils]
	- Max, min, and avg fitness are all graphed on one panel
	- Population is graphed on another panel

## [0.4.0] - 2017-10-31

### Changed
- Complete overhaul of the save system so that long-term runs can be carried out

## [0.3.0] - 2017-09-29

### Added
- Added another thread to compute fitness percentiles of trees
- Added display box for tracked tree in bottom-right corner

### Changed
- Changed color of info/debug text from white to black
- The landscape is now generated with a layered trig function (height is a function of displacement from x=0)

## [0.2.1] - 2017-09-16

### Added
- Multithreading functionality
	- Multithreading can be toggled on by pressing "m"
	- Per tick, one thread computes collisions of SunSpecks with trees,
	while another thread computes collisions of RainDrops with trees

## [0.2.0] - 2017-09-16

### Added
- Partially added GeneSeeds
	- GeneSeeds are seeds that a parent tree drops when it has accumulated enough fitness
	- GeneSeeds that touch the ground create new trees that resemble their parent trees
- Added RainDrops
	- RainDrops are an alternative form of energy collected by new Raincatcher nodes
	- RainDrops fall through every kind of node except RainCatchers
	- Energy is collected by both Leaf and Raincatcher nodes
- Added a display to determine how many ticks occur in one second

### Changed
- A GeneTree now exchanges one energy point and one nutrient point for two fitness points
- A "warmup" now occurs before any particle collisions with trees are calculated
	- During "warmup", the environment is populated with particles

## 0.1.0 - 2017-06-11 (Did not use version control)

### Added
 - Created project as a branch of GeneTrees-v1
 - The simulation world can now be scaled to any width and any height
 - The window can now be resized and panned around in
 - Trees are generated and simulated concurrently

### Changed
 - Structure nodes now consume 4x as much fitness as leaf or root nodes;
 - One sunlight and one nutrient are now converted into 4 fitness

[UrfJavaUtils]: https://github.com/SimNine/UrfJavaUtils

[Unreleased]: https://github.com/SimNine/GeneTrees-v2/compare/v0.8.0...HEAD
[0.8.0]: https://github.com/SimNine/GeneTrees-v2/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/SimNine/GeneTrees-v2/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/SimNine/GeneTrees-v2/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/SimNine/GeneTrees-v2/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/SimNine/GeneTrees-v2/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/SimNine/GeneTrees-v2/compare/v0.2.1...v0.3.0
[0.2.1]: https://github.com/SimNine/GeneTrees-v2/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/SimNine/GeneTrees-v2/commits/v0.2.0
