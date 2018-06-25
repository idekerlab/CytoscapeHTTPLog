# CytoscapeHTTPLog
Convert HTTP logs into download and execution graphs

There are two main() functions in this repository: 

- CytoscapeDownloadCounter.java - processes HTTP logs and produces .TSV files counting Cytoscape version downloads
- CytoscapeNewsCounter.java - processes HTTP logs and produces a .TSV file counting Cytoscape startups

## CytoscapeDownloadCounter

This program will read an HTTP log file that contains Cytoscape downloads and produce product download counts.

To run it, provide two command line parameters:

- Full path to the file containing all HTTP log entries since the beginning of time (... many GBs)
- Full path to an existing directory where output files can be written

The HTTP log files are at /cellar/data/chianti/logs. To prepare them, concatenate all access* file into a 
single file. That file is the first parameter to CytoscapeDownloadCounter.

The following files will be created:
     
**processed.log** -- a list of the HTTP log entries included in the Cytoscape statistics

**product.tsv** -- a breakdown of downloads by Cytoscape product

**productfile.tsv** -- a breakdown of downloads by actual file

**bots.log** -- a list of bots that were inferred and not counted

**sourceIPs.log** -- a list of valid IPs downloaded to

CytoscapeDownloadCounter generates the bots.log file by reading the prevbots.log file to find out what 
bots were previously found. Successive runs of this program should be preceded by copying bots.log to 
prevbots.log.

## CytoscapeNewsCounter

This program will read an HTTP log file that contains Cytoscape downloads and produce startup counts.

To run it, provide two command line parameters:

- Full path to the file containing all HTTP log entries since the beginning of time (... many GBs)
- Full path to an existing directory where output files can be written

The following files will be created:

**news.tsv** -- a breakdown of Cytoscape startups per day

We assume that bots are not skewing the statistics, and so don't account for them.


