import sys
import re
import os
import subprocess
import platform
import shutil
import time
import argparse
import csv


PARSER = argparse.ArgumentParser(description='')
PARSER.add_argument('-o', help='The output directory', dest='out', required=True)
PARSER.add_argument('-m', help='Metric directories', dest='metric', required=True)
PARSER.add_argument('-sm', help='SM directory list file', dest='sm', required=True)

SCRIPTDIR = os.path.abspath(os.path.dirname(sys.argv[0]))

PROJECTS = [
  "antlr4",
  "MapDB",
  "junit",
  "titan",
  "BroadleafCommerce"
]

def mergeCsv(project, sha, smdir, metricDir, type, smSuffix, out):

  dir = os.path.join(metricDir, sha, type)
  
  metrics = dict()
  metricNames = set()
  for m in os.listdir(dir):
    with open(os.path.join(dir, m),'r') as f:
      csvFile = csv.DictReader(f)
      for n in csvFile.fieldnames:
        if n != "name":
          metricField = n
          break
      metricNames.add(metricField)
      for row in csvFile:
        if not row['name'] in metrics:
          metrics[row['name']] = dict()
        metrics[row['name']][metricField] = row[metricField]
        
  outdir = os.path.join(out, sha)
  if not os.path.exists(outdir):
    os.makedirs(outdir)
    
  metricNames = list(metricNames)
    
  with open(os.path.join(smdir, project+"-"+smSuffix+".csv"),'r') as smf:
    with open(os.path.join(out, sha, project+"-"+smSuffix+".csv"),'w') as of:
      smcsv = csv.DictReader(smf)
      ocsv = csv.DictWriter(of, fieldnames = smcsv.fieldnames + metricNames, quotechar='"', quoting=csv.QUOTE_ALL, lineterminator="\n")
      
      ocsv.writeheader()
      for row in smcsv:
        extra = dict()
        for mn in metricNames:
          if row['LongName'] in metrics and mn in metrics[row['LongName']]:
            extra[mn] = metrics[row['LongName']][mn]
          else:
            extra[mn] = "0"
        ocsv.writerow(dict(row.items() | extra.items()))
  

def mergeProject(project, smdirs, input, out):


  releases = dict()
  
  with open(os.path.join("data", project, "traditional.txt"),'r') as f:
    for line in f:
      line = line.rstrip("\n")
      releases[line] = ""
      
  with open(os.path.join(smdirs, project, 'release_directories.txt'),'r') as f:
    for line in f:
      line = line.rstrip("\n")
      t = line.split("\\")
      sha = t[len(t)-1]
      releases[sha] = line
    
  for sha in releases.keys():
    if releases[sha] == "":
      print("Missing release dir for: " + sha)
      continue
      
      #TODO: missing realease dir fix: mapping nincs benne a tradition.txt-ben
    
    mergeCsv(project, sha, releases[sha], os.path.join(input, project), "file", "File", os.path.join(out, project))
    mergeCsv(project, sha, releases[sha], os.path.join(input, project), "method", "Method", os.path.join(out, project))
    mergeCsv(project, sha, releases[sha], os.path.join(input, project), "class", "Class", os.path.join(out, project))
  
  
def main():

  args = PARSER.parse_args()
  
  for project in PROJECTS:
    mergeProject(project, args.sm, args.metric, args.out)

if __name__ == '__main__':
  sys.exit(main())

