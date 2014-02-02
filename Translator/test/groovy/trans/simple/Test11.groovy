
class A { 
  int x
  int y
  
  A(int x, int y) { 
    this.x = x
    this.y = y
  }

}

class B { 
  A a
  float z

  B(A a, int z) { 
    this.a = a
    this.z = z
  }

}

A a1 = new A(100, 200)
println a1.x
println a1.y

B b1 = new B(a1, 1000)
println b1.z
println b1.a.x
println b1.a.y