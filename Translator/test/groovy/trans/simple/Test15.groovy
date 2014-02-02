class A { }

class B extends A { }

def m1(x) { x }
def m2(B b) { b }

A a = new A()
B b = new B()

m1(a)
m1(b)
m2(b)
a = b
m2(a)