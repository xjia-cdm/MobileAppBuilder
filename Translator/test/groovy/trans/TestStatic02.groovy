
class C { 
  static int id = 0; 
  static void next() {
    id++
  }
}

println C.id
C.next()
println C.id
