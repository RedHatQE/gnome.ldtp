#!/usr/bin/python

#This is a short script that diffs two ldtp api files: ldtp_api.clj and ldtp_api2.clj

file1 = open("ldtp_api.clj", "r")
ldtp1 = []
ldtp1commands = []
line = file1.readline().strip()
while line:
	ldtp1.append(line)
	command = line.split("\"")[1]
	ldtp1commands.append(command)
	line = file1.readline()
ldtp1.sort()
ldtp1commands.sort()
file1.close

file2 = open("ldtp_api2.clj", "r")
ldtp2 = []
ldtp2commands = []
line = file2.readline().strip()
while line:
	ldtp2.append(line)
	command = line.split("\"")[1]
  ldtp2commands.append(command)
  line = file2.readline()
ldtp2.sort()
ldtp2commands.sort()
file2.close

uniq1 = []
for item in ldtp1commands:
        if (item in ldtp2commands):
                continue
        else:
                uniq1.append(item)

same = []
uniq2 = []
for item in ldtp2commands:
	if (item in ldtp1commands):
		same.append(item)
	else:
		uniq2.append(item)

print
print "UNIQUE LDTP1 FUNCTIONS"
print uniq1
print
print "UNIQUE LDTP2 FUNCTIONS"
print uniq2
print
print "SHARED FUNCTIONS"
print same
print

functions = []
i = 0;
for item in same:
	functions.append(filter(lambda x: (x.find(item) > -1), ldtp1))
for item in functions:
	print item[0].replace(" (\"","def ").replace("\" [[\"","(self, ").replace("\" \"",", ").replace("\"] 0])","):").replace("\"] 1])","):").replace("\"] 2])","):").replace("\"] 3])","):").replace("\" [[] 0])","():").replace("]","").replace("[",""),
	print "  return (ldtp." + same[i] + "(windowName, componentName) or 0)\n"
	i = i+1
