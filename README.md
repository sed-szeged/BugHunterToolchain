# BugHunterToolchain

Folders:

* cl - comman line tools
* data - project specific data
* lib - neccessary libraries' jar file
* python - utility python scripts

Tools:

* IssueCollector: collects project specific information from GitHub
* IssueMiner: creates statistics from GitHub information
* DataCollector: calculates bug numbers and few process metrics in a custom data structure
* GraphBuilder: creates importable graph database from a project
* GraphExtractor: computes the process metrics in a graph database by running cypher queries

Scripts:

* smbugMerger: merges the outputs of DataCollector to the CSV files
* processMetricMerge: merges the outputs of GraphExtractor to the CSV files

Additional tools:

* SourceMeter: https://www.sourcemeter.com/
* Neo4j: https://neo4j.com/