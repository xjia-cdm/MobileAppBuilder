class B { 
  int y = 200
}

interface I { 
  static final int n = 1000
}

class C extends B implements I {
  int x = 0
  static int a = 100
}

C c = new C()
println c.x
println c.a
println C.a

println c.y
println c.n