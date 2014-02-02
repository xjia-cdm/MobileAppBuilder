
def m1(int i, int j) { 
  i + j
}

def m1(List l, int i) { 
  l << i 
}

println m1(100, 1)

println m1([1, 2, 3], 100)