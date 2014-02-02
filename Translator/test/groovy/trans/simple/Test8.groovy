enum E { e1, e2, e3 }

class A { 

  enum E1 { X, Y, Z }

}

println E.e1
println E.e2
println E.e3

println A.E1.X
println A.E1.Y
println A.E1.Z

for (e in E) 
  println e

for (e in A.E1) 
  println e