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

def m(l) {
  l[0]
}

println m([d]).m1()
println m([b]).m1()
println m([b,c]).m1()
println m([a]).m1()

def p(l) {
  l
}

println p([d])[0].m1()
println p([b])[0].m1()
println p([b,c])[0].m1()
println p([a])[0].m1()