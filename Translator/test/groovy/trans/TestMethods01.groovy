class A {
  int x
  
  public def x1() {
    int y = 1;
    if (x == 1)
      return 4.5 + 5;
    else if (x == 2)
      return 3.2;
    y
  }
   
  public def x2() {
    def a
    a = x3()
    a = x3(2.3)
    return a
  }

  public int x3() {
    return 4;
  }
   
  public double x3(double z) {
    return z;
  }
}

def f1  = 2
def f2 = f1 * 2.0
def f3 = f2 * 1000;

def a = new A()

def b = a.x3()
def c = a.x3(2.4)
def d = a.x2()
def e = a.x1()

println f1
println f2
println f3
println b
println c
println d
println e
