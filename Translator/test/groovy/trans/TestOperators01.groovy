
class A { 
  
  def m() { 
    'A.m'
  }

  def eq(x, y) { 
    x == y
  }
  
  String toString() { 
    'A'
  }
}

def x = 1, y = 2
def a1 = new A()
def a2 = null

println (x == y ? 'T' : 'F')
println a1 
println a1?.m()
println a2?.m()
println a1?: 'Null Pointer'
println a2?: 'Null Pointer'  


