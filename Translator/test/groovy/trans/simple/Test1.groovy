
package pkg1.pkg2

class Test1 { 

  int a, b
  String s = '012'
  String t = 'abc'
  double d = 0.0

  def m1() { 
    a + b
  }

  def m2() { 
    def u = s + t
    ++u 
  }

  static void main(args) { 
    println 'main'
    Test1 t = new Test1()
    t.a = 100
    t.b = 200
    println t.m1()
    println t.m2()
    println t.a
    println t.b
    println t.d
  }

}
