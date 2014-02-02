def head(x) { 
  if (x && x.size > 0) { 
    return x[0]
  }
  null
}

println head([1, 2, 3])
println head([])