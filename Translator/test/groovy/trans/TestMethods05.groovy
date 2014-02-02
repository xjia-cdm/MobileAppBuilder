
class A {  

  def m1(int value, boolean animated = true) { 
    value + (animated ? 1 : 0) 
  }

}

A a = new A()
println a.m1(100, true)
println a.m1(100, false)
println a.m1(100)
