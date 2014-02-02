
class A {
  A() {}
  protected int x = 100
  def m1() { x + 1 }
}

class B extends A {
  int y = 1000
  int z = 1000
  B() { super(); z = 2000 }
  B(int y) { this(); this.y = y }
  def m1 () { x + y + z }
}

A a = new A()
B b1 = new B()
B b2 = new B(2000)

println a.x
println a.m1()
println b1.x
println b1.y
println b1.z
println b1.m1()
println b2.x
println b2.y
println b2.z
println b2.m1()



