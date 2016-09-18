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
PARSER.add_argument('-sm', help='SM csv directory', dest='sm', required=True)
PARSER.add_argument('-b', help='Traditional bug directory', dest='b', required=True)

SCRIPTDIR = os.path.abspath(os.path.dirname(sys.argv[0]))

PROJECTS = [
  "antlr4",
  "MapDB",
  "junit",
  "titan",
  "BroadleafCommerce"
]

def mergeCsv(project, sha, smdir, bugdir, type, smSuffix, out):

  bugfile = os.path.join(bugdir, sha+"-"+type+".csv")
  
  bugs = dict()
  with open(bugfile,'r') as f:
    csvFile = csv.DictReader(f)
    for row in csvFile:
      bugs[row['lim_id']] = row['Number of bugs']
        
  if not os.path.exists(out):
    os.makedirs(out)
    
  with open(os.path.join(smdir, project+"-"+smSuffix+".csv"),'r') as smf:
    with open(os.path.join(out, project+"-"+smSuffix+".csv"),'w') as of:
      smcsv = csv.DictReader(smf)
      fieldnames = smcsv.fieldnames + ['Number of bugs']
      ocsv = csv.DictWriter(of, fieldnames = fieldnames, quotechar='"', quoting=csv.QUOTE_ALL, lineterminator="\n")
      
      ocsv.writeheader()
      for row in smcsv:
        if row['ID'] in bugs:
          row['Number of bugs'] = bugs[row['ID']]
        else:
          row['Number of bugs'] = '0'
        ocsv.writerow(row)
  

def mergeProject(project, smdirs, bugs, out):
    
  for sha in os.listdir(smdirs):
    mergeCsv(project, sha, os.path.join(smdirs, sha), os.path.join(bugs, 'traditional'), "file", "File", os.path.join(out, sha))
    mergeCsv(project, sha, os.path.join(smdirs, sha), os.path.join(bugs, 'traditional'), "element", "Method", os.path.join(out, sha))
    mergeCsv(project, sha, os.path.join(smdirs, sha), os.path.join(bugs, 'traditional'), "element", "Class", os.path.join(out, sha))
  
  
def main():

  args = PARSER.parse_args()
  
  for project in PROJECTS:
    mergeProject(project, os.path.join(args.sm, project), os.path.join(args.b, project), os.path.join(args.out, project))

if __name__ == '__main__':
  sys.exit(main())

