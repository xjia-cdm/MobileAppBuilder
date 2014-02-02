class A { 

  int x
  IA1 c = new IA1()

  class IA1 { 
    int y

    void m2() { 
      println "m2: x=" + x
      println "m2: y=" + y
    }

  }

  void m1() { 
    println "m1: x=" + x
    println "m1: c.y=" + c.y
  }

}

A a = new A()
a.x = 10
a.c.y = 20
a.m1()
println a.x
a.c.m2()
println a.c.y
