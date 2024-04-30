# Distributed Graph Coloring in Java

## Introduction
In this assignment, I have implemented a distributed graph coloring algorithm using Java. The objective was to color a graph such that no two adjacent nodes share the same color using exactly 1 + Δ colors, where Δ is the maximum degree of any node in the graph. Each node autonomously determines its color by communicating with its neighbors.


## Input and Output Formats

### Input File Structure
The input file consists of the following structured data:
- **First Line**: Number of nodes (`numNodes`).
- **Second Line**: Maximum degree in the graph (`maximalDegree`).
- **Subsequent Lines**: Each line represents a node and lists its neighbors, along with the writing and reading ports for communication, formatted as:
  ```
  nodeID [[neighborId, writingPort, readingPort], …]
  ```

#### Example Input File
```
5
3
0 [[1, 6060, 13821], [2, 6067, 11111]]
1 [[0, 13821, 6060], [2, 6069, 13131]]
2 [[0, 11111, 6067], [1, 13131, 6069], [3, 12312, 70809]]
3 [[2, 70809, 12312], [4, 67679, 10101]]
4 [[3, 10101, 67679]]
```

### Output File Structure
The output file should list the nodes in ascending order with their respective colors, formatted as:
```
nodeId,nodeColor
nodeId,nodeColor
...
```

#### Example Output
```
0,0
1,1
2,2
3,0
4,3
```

## Classes and Responsibilities

### Pair Class
- A generic class representing a key-value pair.
- Implements `Serializable` for network transmission.

### Node Class
- Represents a graph node.
- Extends `Thread` or implements `Runnable` for distributed execution.
- Manages node ID, neighbors, and communicates with adjacent nodes to determine color.

### Manager Class
- Handles the initial reading and parsing of the input file.
- Initiates the graph setup and starts the coloring process.
- Manages the orderly termination of the process and outputs the results.

### Main Class
- The entry point for running the coloring algorithm.
- Manages the lifecycle of the `Manager` class operations.

## Communication Protocol
- Direct communication must be established between neighboring nodes using the specified ports.
- Only neighboring nodes can exchange messages, ensuring the algorithm runs in a truly distributed manner.

