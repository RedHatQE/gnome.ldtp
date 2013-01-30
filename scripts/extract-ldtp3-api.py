#!/usr/bin/python
import inspect
import ldtp
from ldtpd.core import Ldtpd

#This program is to extract the api out of the loaded ldtp library using introspection.
#This is mostly helpful whenever ldtp updates or a new version comes out.
#This section works on ldtp v3

def defaultCount(spec):
  x = spec[3]
  if (x):
    return len(x)
  else:
    return 0

def checkValid(symbol):
    if symbol.startswith('_'):
        return False
    obj = getattr(Ldtpd, symbol)
    if not callable(obj):
        return False
    return True

names = filter(lambda fn: checkValid(fn), dir(Ldtpd))
specs = map(lambda n: inspect.getargspec(getattr(Ldtpd, n)), names)
map(lambda s: s[0].remove('self'), specs)
argz  = map(lambda s: [ s[0], defaultCount(s)], specs)
api = zip(names, argz)
print(str(api).replace("'", '"'))

