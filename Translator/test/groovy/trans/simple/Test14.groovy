class A { 

  class IA1 { 
    int a

    def test() { this }
    def test2() { A.this }
  }

  static class IA2 { 
    float a

    def test() { this }
  }

  IA1 a1 = new IA1()
  IA2 a2 = new IA2()

  def test() { this }

}

A a = new A()
a.a1.a = 100
println a.a1.a