#!/usr/bin/python
import inspect
import ldtp

#This program is to extract the api out of the loaded ldtp library using introspection.
#This is mostly helpful whenever ldtp updates or a new version comes out.

#xmlrpc = XMLRPCLdtpd()
names = filter(lambda fn: inspect.isfunction(getattr(ldtp,fn)),  dir(ldtp))
#names = map(lambda n: n.split("xmlrpc_")[1], public_apis) #strip prefix off fn names
specs = map(lambda n:  inspect.getargspec(getattr(ldtp,n)), names)
#print specs
def defaultCount(spec):
  x = spec[3]
  if (x): 
    return len(x)
  else:
    return 0
argz  = map(lambda s: [ s[0], defaultCount(s)], specs)
api = zip(names, argz)
print(str(api).replace("'", '"'))
