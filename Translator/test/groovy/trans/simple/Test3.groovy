package xj.pkg2

enum E { e1, e2, e3 }

class A { 

  enum E1 { X, Y, Z }

  class IA1 { 
    int a
  }

  static class IA2 { 
    float a
  }

  E1 enum1 = E1.X
  IA1 a1 = new IA1()
  IA2 a2 = new IA2()

}

class B { 
  A a;
  B b;

  public void m1() {
    println "B -> " + m2(100, 200)
  }

  protected int m2(int x, int y) { 
    return x + y
  }

  def m3(a) { 
    a
    //a++
  }

}

println E.e1
println E.e2
println E.e3

println A.E1.X
println A.E1.Y
println A.E1.Z

A a = new A()
println a.enum1
a.a1.a = 100
println a.a1.a

B b = new B()
b.m1()