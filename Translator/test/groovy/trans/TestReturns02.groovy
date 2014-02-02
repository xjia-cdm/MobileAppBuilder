
// test statements

def m1(int x) {
  int i = 10, j = 20
  if (x < 10) {
    j
  } else {
    i
  }
}

def m2(int x) {
  int i = 0, j = 10
  i += 20 
  x + i
}

def m3(int x) {
  if (x < 10) {
    x++; 
    x++; 
    x 
  } else if (x > 30) {
    switch (x) {
    case 31: x + 200; break;
    case 32: case 33: return x + 300;
    default: x + 400; 
    }
  } else if (x > 20) {
    x 
  } else {
    x + 100
  }
}

def m4(String s) {
  def v = 0
  synchronized (s) {
    try {
      v = Integer.parseInt(s)
    } catch (NumberFormatException e) {
      e.printStackTrace()
    } catch (Exception e) {
      e.printStackTrace()
    } finally {
      v = 0
    }
  }
}

def m5(int x) {
  def v = 1
  while (x++ < 10) {
    v *= 2
  }
  v
}

def m6(int x) {
  def v = 1
  for (int i = 0; i < x; i++) {
    v *= 2
  }
  v
}

println m1(5)
println m1(15)
println m2(0);
	
println m3(5)
println m3(15)
println m3(25);
println m3(31)
println m3(32)
println m3(33)
println m3(35)

println m5(5)
println m6(5)



