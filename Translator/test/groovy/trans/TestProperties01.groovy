 class A {
   int x
   def y
 }
 def a = new A()
 println a.x
 a.x = 1 + 10
 println a.x

 def b = a.x
 println a.x
 //int b1 = a.x
 a.x += b
 //a.x++
 println a.x