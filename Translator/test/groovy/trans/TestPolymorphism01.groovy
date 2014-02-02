class A { 
  def m1() { "A.m1"}
}

class B extends A { 
  def m1() { "B.m1" }
}

class C extends A { 
  def m1() { "C.m1" }
}

class D {
  def m1() { "D.m1" }
}

A a = new A()
B b = new B()
C c = new C()
D d = new D()

def m(x) {
  x.m1()
}

println m(b) //Will make a copy
println m(c) //Will make a copy
println m(d) //WIll make a copy
println m(a) //Will override m(b) and delete the un-needed m(c)

