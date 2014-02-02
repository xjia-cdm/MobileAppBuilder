
class A { 

  class IA1 { 
    int a
  }

  static class IA2 { 
    float a
  }

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


A a = new A()
a.a1.a = 100
println a.a1.a

B b = new B()
b.m1()