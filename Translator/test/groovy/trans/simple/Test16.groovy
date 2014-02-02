
class A { 

  def m1(A a, int i) { 
    println "m1 " + i
  }

  def m2(int i) { 
    println "m2 " + i
    m1(this, i++)
  }
  
}

A a = new A()
a.m2(100)