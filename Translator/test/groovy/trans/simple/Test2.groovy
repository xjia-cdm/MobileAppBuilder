package xj.pkg1

class C1 { 
  A a
  String toString() { "C1" }
}

class A { 
  B b
  String toString() { "A" }
}

class B { 
  C c
  String toString() { "B" }
}

class C { 
  //D x
  String toString() { "C" }
}

class D { 
  String toString() { "D" }
}

interface I1 { 
  int CONST =100
}


interface I2 { 
  int m1()
}

class AA extends A { 
  String toString() { "AA" }
}

class BB extends B implements I1 { 
  String toString() { "BB" }
}

class CC extends C implements I1, I2 { 
  D x
  String toString() { "CC" }
  int m1() { 1 }
}

A a = new A()
B b = new B()
C c = new C()
D d = new D()
C1 c1 = new C1()

AA aa = new AA()
BB bb = new BB()
CC cc = new CC()

println a
println b
println c
println d
println c1

println aa
println bb
println bb.CONST
println BB.CONST
println cc
println cc.CONST
println CC.CONST
println cc.m1()

