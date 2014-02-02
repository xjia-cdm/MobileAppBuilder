/*
	Acyclic dependency
	Script -> A, B, C
	A -> D
	B -> D
	C -> B
	D (none)

*/

  class A {     
	public def x() {
	  def d = new D()
	  return d.x()
	}
  } 
 
  class B {
 	public def x() {
 	  def d = new D()
 	  return d.x()
 	}
  }
 
  class C {
    B b = new B()
 	public def x() {
 	  return "Yes"
 	}
  }
  
  class D {
    public def x() {
      return 4
    }
    public static x2() {
      return 5
    }
  }
  def z = D.x2()
  def a = new A()
  def a1 = a.x() //Should be type String
  
  def b = new B()
  def b1 = b.x() //Should be type String
  
  def c = new C()
  def c1 = c.x() //Should be type int

  println z
  println a1
  println b1
  println c1
