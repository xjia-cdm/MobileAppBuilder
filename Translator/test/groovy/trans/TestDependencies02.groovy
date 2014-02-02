/*
	Cyclic dependency
	Script -> A, B, C
	A -> D
	B -> D
	C (none)
	D -> A

*/

  class A {   
	public def x() {
	  def d = new D()
	  return d.x()
	}
	
	public def x2() {
	  def d = new D()
	  return d.x2()
	}
  } 
 
  class B {
 	public def x() {
 	  def d = new D()
 	  return d.x()
 	}
  }
 
  class C {
 	public def x() {
 	  return "Yes"
 	}
  }
  
  class D {
    public def x() {
      return 4
    }
    
    public def x2() {
      def a = new A()
      return a.x()
    }
  }

  def a = new A()
  def a1 = a.x() //Should be type int
  def a2 = a.x2() //Should also be int
  
  def b = new B()
  def b1 = b.x() //Should be type int
  
  def c = new C()
  def c1 = c.x() //Should be type String

  println a1
  println a2
  println b1
  println c1