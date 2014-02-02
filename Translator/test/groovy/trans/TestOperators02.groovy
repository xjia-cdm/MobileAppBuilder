
def x = 1, y = 2, z = 100
def map = [x : 1, y : 2, z : 3]

z += x
println z

map['z'] = 100
println map

z = map['z']
println z   