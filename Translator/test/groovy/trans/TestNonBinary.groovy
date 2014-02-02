def x = -100 //int
def x1 = -1.2 //float
def x2 = 100++ //int

def y = x > 0 ? 1.2 : 1 //float
def y1 = x < 0 ? 1.2 : true //Object

def z = "hi" * 2 //String
def z1 = "hi"++ //String
def z2 = "hi" < "hello"  //Boolean
def z4 = 1 + "hi"

def a1 = 1 < 2 //Boolean

def b = [2,3,4, "hi"]  //List
def b1 = [2,3,4] + 5
def b2 = [2,3,4] - 4
def b3 = [2,3,4] * 2

println x
println x1
println x2

println y
println y1

println z
println z1
println z2
println z4

println a1

println b
println b1
println b2
println b3
