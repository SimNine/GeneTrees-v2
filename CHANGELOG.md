<!-- markdownlint-disable-file MD024 -->

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

This project **does not** adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This is a branch of GeneTrees-v1, forked after its 0.4.0 tag. Where GeneTrees-v1 simulated only one tree at a time, GeneTrees-v2 simulates many trees at once, thus allowing interaction between trees.

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

## [0.1.0] - 2017-06-11 (FORGOT TO COMPILE)

### Added
 - Created project as a branch of GeneTrees-v1
 - The simulation world can now be scaled to any width and any height
 - The window can now be resized and panned around in
 - Trees are generated and simulated concurrently

### Changed
 - Structure nodes now consume 4x as much fitness as leaf or root nodes; 
 - One sunlight and one nutrient are now converted into 4 fitness