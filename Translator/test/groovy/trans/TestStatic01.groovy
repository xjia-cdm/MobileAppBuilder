
class C { 
  static int i = 0; 
  static void next() {
    i++
  }
}

println C.i
C.next()
println C.i
