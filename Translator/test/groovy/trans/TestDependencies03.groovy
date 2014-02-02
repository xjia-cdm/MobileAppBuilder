/*
    Acyclic dependency
    Script -> A, B, C
    A -> D
    B -> D
    C -> B
    D (none)

	Sorted order: Script, A, C, B, D
*/

  class A {     
    public def x() {
      def d = new D()
      if (true) {
          return d.x()
      } else {  
          return d.y()
      }
    }
  } 
 
  class B {
     public def x() {
       def d = new D()
       if (false) {
	 return d.z()
       } else {
         if (false) {
	   return d.x()
         } else if (true) {
	   return d.y()
         } 
	 return d.z()	 
       }
     }
  }
 
  class C {
    B b = new B()
    public def x() {
      return "Yes"
    }
    public def y() {
      return "No"
    }
  }
  
  class D {
    public def x() {
      return 4
    }
    public def y() {
      return 5
    }
    public def z() {
      return 6
    }
    public static x2() {
      return 5
    }
  }

  try {
      def a = new A()
      def a1 = a.x() //Should be type String
      println a1
  
  } catch(Exception e) {
      def b = new B()
      def b1 = b.x() //Should be type String
      println b1
  }

def b = new B()
def b1 = b.x() //Should be type String
println b1
  
  for (int i=0; i<10; i++) {
      def c = new C()
      def c1 = c.x() //Should be type int
      println c1 + ' ' + i
  }