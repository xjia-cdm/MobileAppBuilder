
class A { 
  def m1() { "A.m1"}
}

class B extends A { 
  def m2() { "B.m2" }
}

class C extends A { 
  def m3() { "C.m3" }
}

def a = new A()
def b = new B()
def c = new C()

println a.m1()
println b.m2()
println c.m3()

void m1(x) { 
  if (x instanceof B) { 
    println "is B"
    println x.m2()
  } else if (x instanceof C) { 
    println "is C"
    println x.m3()
  } else if (x instanceof A) { 
    println "is A"
    println x.m1()
  } else {  
    println "is Object"
  }
}

m1(a)
m1(b)
m1(c)

void m2(x) { 
  switch (x.class) { 
  case B:
    println "is B"
    println x.m2()
    break;

  case C:
    println "is C"
    println x.m3()
    break;

  case A:
    println "is A"
    println x.m1()
    break

  default:
    println "is Object"
  }
}

m2(a)
m2(b)
m2(c)

void m3(x) { 
  if (x.class == B) { 
    println "is B"
    println x.m2()
  } else if (x.class == C) { 
    println "is C"
    println x.m3()
  } else if (x.class == A) { 
    println "is A"
    println x.m1()
  } else {  
    println "is Object"
  }
}

m3(a)
m3(b)
m3(c)