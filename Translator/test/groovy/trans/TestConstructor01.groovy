
class A { 

  int x, y, z

  String toString() { 
    "[x: ${x}, y: ${y}, z: ${z}]"
  }
}

def a1 = new A(x: 1, y: 2, z: 3)
def a2 = new A(x: 1, y: 2)
def a3 = new A(z: 1, x: 2)

println a1
println a2
println a3